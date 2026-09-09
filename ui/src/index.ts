import {definePlugin} from '@halo-dev/ui-shared'
import {IconDashboard, IconGrid, IconRocketLine} from '@halo-dev/components'
import {markRaw} from 'vue'
import RiHeartLine from '~icons/ri/heart-line'
import RiGalleryLine from '~icons/ri/gallery-line'
import RiCheckboxMultipleLine from '~icons/ri/checkbox-multiple-line'
import RiBook2Line from '~icons/ri/book-2-line'
import RiMegaphoneLine from '~icons/ri/megaphone-line'
import RiLink from '~icons/ri/link'
import RiFileList3Line from '~icons/ri/file-list-3-line'
import RiShieldCheckLine from '~icons/ri/shield-check-line'
import RiSlideshowLine from '~icons/ri/slideshow-line'
import RiSettings3Line from '~icons/ri/settings-3-line'
import WelcomeView from './views/WelcomeView.vue'

function baseRoutePath(path: string) {
  return `/uni-halo${path}`
}

const GROUP_NAME = 'UniHalo v3.x'

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
            group: GROUP_NAME,
            icon: markRaw(IconDashboard),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/general-config'),
        name: 'GeneralConfig',
        component: () => import('@/views/general-config/GeneralConfigView.vue'),
        meta: {
          title: '通用配置',
          searchable: true,
          hideFooter: false,
          permissions: ["plugin:uni-halo:general-config:view"],
          menu: {
            name: '通用配置',
            group: GROUP_NAME,
            icon: markRaw(RiSettings3Line),
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
            group: GROUP_NAME,
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
            group: GROUP_NAME,
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
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/notice'),
        name: 'NoticeList',
        component: () => import('@/views/notice-manage/NoticeListView.vue'),
        meta: {
          title: '公告管理',
          searchable: true,
          hideFooter: false,
          permissions: ["plugin:uni-halo:notice:view"],
          menu: {
            name: '公告管理',
            group: GROUP_NAME,
            icon: markRaw(RiMegaphoneLine),
            priority: 3,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/links'),
        name: 'LinkManage',
        component: () => import('@/views/link-manage/LinkManageLayout.vue'),
        redirect: baseRoutePath('/links/list'),
        meta: {
          title: '链接管理',
          searchable: false,
          hideFooter: false,
          permissions: ["plugin:uni-halo:link:view"],
          menu: {
            name: '链接管理',
            group: GROUP_NAME,
            icon: markRaw(RiLink),
            priority: 4,
          },
        },
        children: [
          {
            path: 'list',
            name: 'LinkList',
            component: () => import('@/views/link-manage/LinkListView.vue'),
            meta: {
              title: '链接列表',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:link:view"],
              menu: {
                name: '链接列表',
                icon: markRaw(RiLink),
                priority: 0,
              },
            },
          },
          {
            path: 'submissions',
            name: 'LinkSubmissions',
            component: () => import('@/views/link-manage/SubmissionListView.vue'),
            meta: {
              title: '申请审核',
              searchable: true,
              hideFooter: false,
              permissions: ["plugin:uni-halo:link:view"],
              menu: {
                name: '申请审核',
                icon: markRaw(RiFileList3Line),
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
        path: baseRoutePath('/audit-config'),
        name: 'AuditConfig',
        component: () => import('@/views/audit-config/AuditConfigView.vue'),
        meta: {
          title: '审核配置',
          searchable: true,
          hideFooter: false,
          permissions: ["plugin:uni-halo:audit-data:view"],
          menu: {
            name: '审核配置',
            group: GROUP_NAME,
            icon: markRaw(RiShieldCheckLine),
            priority: 5,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/banners'),
        name: 'BannerList',
        component: () => import('@/views/banner-manage/BannerListView.vue'),
        meta: {
          title: '轮播管理',
          searchable: true,
          hideFooter: false,
          permissions: ["plugin:uni-halo:banner:view"],
          menu: {
            name: '轮播管理',
            group: GROUP_NAME,
            icon: markRaw(RiSlideshowLine),
            priority: 6,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})
