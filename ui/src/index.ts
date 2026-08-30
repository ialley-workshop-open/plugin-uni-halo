import {definePlugin} from '@halo-dev/ui-shared'
import {IconPlug, IconSettings} from '@halo-dev/components'
import {markRaw} from 'vue'
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
            group: 'uni-halo',
            icon: markRaw(IconSettings),
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
        component: () => import('@/views/AppInfoListView.vue'),
        meta: {
          title: '应用管理',
          searchable: true,
          hideFooter: false,
          permissions: [],
          menu: {
            name: '应用管理',
            group: 'uni-halo',
            icon: markRaw(IconSettings),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: baseRoutePath('/app-versions'),
        name: 'AppVersionList',
        component: () => import('@/views/AppVersionListView.vue'),
        meta: {
          title: '版本管理',
          searchable: true,
          hideFooter: false,
          permissions: [],
          menu: {
            name: '版本管理',
            group: 'uni-halo',
            icon: markRaw(IconPlug),
            priority: 1,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})
