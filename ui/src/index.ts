import {definePlugin} from '@halo-dev/ui-shared'
import {IconDashboard, IconGrid, IconRocketLine} from '@halo-dev/components'
import {markRaw} from 'vue'
import RiHeartLine from '~icons/ri/heart-line'
import RiGalleryLine from '~icons/ri/gallery-line'
import RiCheckboxMultipleLine from '~icons/ri/checkbox-multiple-line'
import RiBook2Line from '~icons/ri/book-2-line'
import WelcomeView from './views/WelcomeView.vue'

function baseRoutePath(path: string) {
  return `/uni-halo${path}`
}

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/welcome'),
        name: 'Welcome',
        component: WelcomeView,
        meta: {
          title: '欢迎使用',
          searchable: true,
          hideFooter: true,
          permissions: [],
          menu: {
            name: '欢迎使用',
            group: 'UniHalo',
            icon: markRaw(IconDashboard),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/app-manage'),
        name: 'AppManage',
        component: () => import('@/views/app-manage/AppManageLayout.vue'),
        redirect: baseRoutePath('/app-manage/list'),
        meta: {
          title: '应用管理',
          searchable: false,
          hideFooter: false,
          permissions: ["plugin:uni-halo:app:view"],
          menu: {
            name: '应用管理',
            group: 'UniHalo',
            icon: markRaw(IconGrid),
            priority: 0,
          },
        },
        children: [
          {
            path: 'list',
            name: 'AppManageList',
            component: () => import('@/views/app-manage/AppInfoListView.vue'),
            meta: {
              title: '应用列表',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:app:view"],
              menu: {
                name: '应用列表',
                icon: markRaw(IconGrid),
                priority: 0,
              },
            },
          },
          {
            path: 'versions',
            name: 'AppVersionList',
            component: () => import('@/views/app-manage/AppVersionListView.vue'),
            meta: {
              title: '版本管理',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:version:view"],
              menu: {
                name: '版本管理',
                icon: markRaw(IconRocketLine),
                priority: 1,
              },
            },
          },
        ],
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/love'),
        name: 'LoveManage',
        component: () => import('@/views/love-manage/LoveManageLayout.vue'),
        redirect: baseRoutePath('/love/config'),
        meta: {
          title: '恋爱管理',
          searchable: false,
          hideFooter: false,
          permissions: ["plugin:uni-halo:love:view"],
          menu: {
            name: '恋爱管理',
            group: 'UniHalo',
            icon: markRaw(RiHeartLine),
            priority: 2,
          },
        },
        children: [
          {
            path: 'config',
            name: 'LoveConfig',
            component: () => import('@/views/love-manage/LoveConfigView.vue'),
            meta: {
              title: '恋爱配置',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:love:view"],
              menu: {
                name: '恋爱配置',
                icon: markRaw(RiHeartLine),
                priority: 0,
              },
            },
          },
          {
            path: 'albums',
            name: 'LoveAlbums',
            component: () => import('@/views/love-manage/LoveAlbumListView.vue'),
            meta: {
              title: '恋爱相册',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:love:view"],
              menu: {
                name: '恋爱相册',
                icon: markRaw(RiGalleryLine),
                priority: 1,
              },
            },
          },
          {
            path: 'daily',
            name: 'LoveDaily',
            component: () => import('@/views/love-manage/LoveDailyListView.vue'),
            meta: {
              title: '恋爱清单',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:love:view"],
              menu: {
                name: '恋爱清单',
                icon: markRaw(RiCheckboxMultipleLine),
                priority: 2,
              },
            },
          },
          {
            path: 'stories',
            name: 'LoveStories',
            component: () => import('@/views/love-manage/LoveStoryListView.vue'),
            meta: {
              title: '恋爱故事',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:love:view"],
              menu: {
                name: '恋爱故事',
                icon: markRaw(RiBook2Line),
                priority: 3,
              },
            },
          },
        ],
      },
    },
  ],
  extensionPoints: {},
})
