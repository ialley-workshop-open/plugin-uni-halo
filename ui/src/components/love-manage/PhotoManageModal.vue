<script setup lang="ts">
import {Toast, VButton, VModal, VSpace} from "@halo-dev/components";
import RiArrowUpLine from "~icons/ri/arrow-up-line";
import RiArrowDownLine from "~icons/ri/arrow-down-line";
import {useQueryClient} from "@tanstack/vue-query";
import {computed, reactive, ref, watch} from "vue";
import {loveAlbumsApi} from "@/api";
import type {LoveAlbum, LoveAlbumPhoto} from "@/types";
import {useWindowSize} from "@vueuse/core";

const props = defineProps<{
  album: LoveAlbum;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const busy = ref(false);
/** 新选择的待添加图片 URL */
const pendingUrls = ref<string[]>([]);
/** 相册照片本地副本 */
const photos = ref<LoveAlbumPhoto[]>([]);
/** 网格选中照片索引（-1 = 添加模式；>=0 = 编辑该照片） */
const selectedIndex = ref(-1);

const {width: windowWidth} = useWindowSize();
const modalWidth = computed(() => Math.round(windowWidth.value * 0.8));

/** 字段表单：标题 / 地点 / 日期 / 描述 */
const form = reactive({
  title: "",
  location: "",
  takenDate: "",
  description: "",
});

const isEditMode = computed(() => selectedIndex.value >= 0);

const queryClient = useQueryClient();

watch(
        () => props.album,
        (album) => {
          photos.value = (album.spec.photos || []).map((photo) => ({...photo}));
          selectedIndex.value = -1;
        },
        {
          immediate: true,
        }
);

// 切换选中照片时，把照片字段载入表单，并回显当前照片到附件组件（可预览/更换）
watch(selectedIndex, (index) => {
  const photo = index >= 0 ? photos.value[index] : undefined;
  form.title = photo?.title || "";
  form.location = photo?.location || "";
  form.takenDate = photo?.takenDate || "";
  form.description = photo?.description || "";
  if (index >= 0) {
    pendingUrls.value = photo?.url ? [photo.url] : [];
  }
});

const refreshList = () => {
  queryClient.invalidateQueries({queryKey: ["uni-halo:love-albums"]});
};

const handleClearForm = () => {
  form.title = "";
  form.location = "";
  form.takenDate = "";
  form.description = "";
};

/** 清空已选择图片 + 表单内容（并退出编辑选中态） */
const handleClearAll = () => {
  pendingUrls.value = [];
  handleClearForm();
  selectedIndex.value = -1;
};

/**
 * 保存：编辑模式 → 更新选中照片字段并整体提交；添加模式 → 所选图片
 * 逐张添加（携带表单字段值）。
 */
const handleSave = async () => {
  try {
    busy.value = true;
    if (isEditMode.value) {
      const photo = photos.value[selectedIndex.value];
      if (!photo) {
        return;
      }
      Object.assign(photo, {
        title: form.title,
        location: form.location,
        takenDate: form.takenDate,
        description: form.description,
      });
      // 附件组件回显了当前照片：若用户更换/新增了图片则同步更新 URL
      const pendingUrl = pendingUrls.value[0];
      if (pendingUrl && pendingUrl !== photo.url) {
        photo.url = pendingUrl;
      }
      await loveAlbumsApi.updatePhotos(props.album.metadata.name, photos.value);
      selectedIndex.value = -1;
      pendingUrls.value = [];
      Toast.success("保存成功");
    } else {
      const urls = pendingUrls.value.filter((url) => url);
      if (!urls.length) {
        Toast.warning("请先选择图片");
        return;
      }
      for (const url of urls) {
        await loveAlbumsApi.addPhoto(props.album.metadata.name, {
          url,
          title: form.title,
          location: form.location,
          takenDate: form.takenDate,
          description: form.description,
        });
      }
      pendingUrls.value = [];
      handleClearForm();
      Toast.success("添加成功");
    }
    refreshList();
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    busy.value = false;
  }
};

const handleEditPhoto = (index: number) => {
  selectedIndex.value = index;
};

const handleDeletePhoto = async (photo: LoveAlbumPhoto) => {
  if (!photo.name) {
    return;
  }
  try {
    busy.value = true;
    await loveAlbumsApi.deletePhoto(props.album.metadata.name, photo.name);
    if (selectedIndex.value >= 0 && photos.value[selectedIndex.value] === photo) {
      selectedIndex.value = -1;
      pendingUrls.value = [];
    }
    Toast.success("删除成功");
    refreshList();
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    busy.value = false;
  }
};

const movePhoto = (index: number, direction: -1 | 1) => {
  const target = index + direction;
  if (target < 0 || target >= photos.value.length) {
    return;
  }
  const list = [...photos.value];
  const [moved] = list.splice(index, 1);
  if (!moved) {
    return;
  }
  list.splice(target, 0, moved);
  photos.value = list;
};

const handleSaveOrder = async () => {
  try {
    busy.value = true;
    await loveAlbumsApi.updatePhotos(props.album.metadata.name, photos.value);
    Toast.success("排序已保存");
    refreshList();
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    busy.value = false;
  }
};
</script>

<template>
  <VModal
          ref="modal"
          :width="modalWidth"
          height="80vh"
          :title="`照片管理：${props.album.spec.displayName || '未命名相册'}`"
          @close="emit('close')"
  >
    <div class=":uno: flex h-full w-full flex-col gap-4 md:flex-row">
      <!-- 左：新增/编辑表单区（内容超出时自身滚动） -->
      <div class=":uno: w-full shrink-0 overflow-y-auto md:w-96">
        <div class=":uno: rounded-md border border-gray-200 p-4">
          <div class=":uno: mb-3 text-sm font-semibold text-gray-700">
            {{ isEditMode ? "编辑照片信息" : "添加新照片" }}
            <span v-if="!isEditMode" class=":uno: ml-1 text-xs font-normal text-gray-400">
              （选择图片并填写信息后保存）
            </span>
          </div>
          <FormKit
                  v-model="pendingUrls"
                  type="attachment"
                  multiple
                  :accepts="['image/*']"
                  label="选择图片"
                  help="从附件库选择多张图片，或直接输入图片地址"
          />
          <div class=":uno: mt-4 space-y-3 w-full">
            <div class=":uno: flex items-center gap-3 w-full">
              <label class=":uno: w-14 shrink-0 text-sm text-gray-700">标题</label>
              <FormKit v-model="form.title" type="text" placeholder="照片标题"
                       class=":uno: flex-1"/>
            </div>
            <div class=":uno: flex items-center gap-3 w-full">
              <label class=":uno: w-14 shrink-0 text-sm text-gray-700">地点</label>
              <FormKit v-model="form.location" type="text" placeholder="拍摄地点"
                       class=":uno: flex-1"/>
            </div>
            <div class=":uno: flex items-center gap-3 w-full">
              <label class=":uno: w-14 shrink-0 text-sm text-gray-700">日期</label>
              <FormKit v-model="form.takenDate" type="date" placeholder="选择拍摄日期"
                       class=":uno: flex-1"/>
            </div>
            <div class=":uno: flex items-start gap-3 w-full">
              <label class=":uno: w-14 shrink-0 text-sm text-gray-700">描述</label>
              <FormKit v-model="form.description" type="textarea" rows="3" placeholder="照片描述"
                       class=":uno: flex-1"/>
            </div>
          </div>
          <div class=":uno: mt-4 flex items-center gap-2">
            <VButton :loading="busy" type="primary" @click="handleSave">
              {{ isEditMode ? "保存修改" : "保存" }}
            </VButton>
            <VButton type="secondary" plain :disabled="busy" @click="handleClearAll">
              清空表单
            </VButton>
          </div>
        </div>
      </div>

      <!-- 右：照片列表（内部滚动），顶部标题 + 保存排序 -->
      <div class=":uno: flex min-h-0 min-w-0 flex-1 flex-col">
        <div class=":uno: mb-3 flex shrink-0 items-center justify-between">
          <div class=":uno: text-sm font-semibold text-gray-700">
            照片列表（{{ photos.length }}）
          </div>
          <VButton
                  v-if="!isEditMode && photos.length"
                  :loading="busy"
                  type="secondary"
                  :disabled="busy"
                  @click="handleSaveOrder"
          >
            保存排序
          </VButton>
        </div>
        <div class=":uno: min-h-0 flex-1 overflow-y-auto pr-1">
          <div v-if="photos.length" class=":uno: grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div
                    v-for="(photo, index) in photos"
                    :key="photo.name || index"
                    class=":uno: overflow-hidden rounded-lg border transition-shadow"
                    :class="
                index === selectedIndex
                  ? ':uno: border-pink-400 ring-2 ring-pink-200'
                  : ':uno: border-gray-200 hover:border-gray-300'
              "
            >
              <img :src="photo.url" :alt="photo.title || ''" loading="lazy"
                   class=":uno: h-24 w-full object-cover"/>
              <div class=":uno: space-y-0.5 bg-gray-50 px-2 py-1.5">
                <div v-if="photo.title" class=":uno: truncate text-xs text-gray-700">
                  {{ photo.title }}
                </div>
                <div class=":uno: flex items-center gap-1 text-[10px] text-gray-400">
                  <span v-if="photo.takenDate">{{ photo.takenDate }}</span>
                  <span v-if="photo.location">📍 {{ photo.location }}</span>
                </div>
              </div>
              <div class=":uno: flex items-center justify-between gap-1 bg-gray-50 px-2 py-1">
                <div class=":uno: flex items-center gap-1">
                  <VButton size="sm" type="secondary" plain :disabled="index === 0"
                           @click="movePhoto(index, -1)">
                    <template #icon>
                      <RiArrowUpLine/>
                    </template>
                  </VButton>
                  <VButton
                          size="sm"
                          type="secondary"
                          plain
                          :disabled="index === photos.length - 1"
                          @click="movePhoto(index, 1)"
                  >
                    <template #icon>
                      <RiArrowDownLine/>
                    </template>
                  </VButton>
                </div>
                <div class=":uno: flex items-center gap-1">
                  <VButton size="sm" type="secondary" plain @click="handleEditPhoto(index)">
                    编辑
                  </VButton>
                  <VButton size="sm" type="danger" plain @click="handleDeletePhoto(photo)">
                    删除
                  </VButton>
                </div>
              </div>
            </div>
          </div>
          <div v-else class=":uno: py-10 text-center text-sm text-gray-400">
            还没有照片，先在左侧选择图片添加吧
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
