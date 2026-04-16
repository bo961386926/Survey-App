import { config } from '@vue/test-utils'

config.global.mocks = {
  $router: {
    push: jest.fn(),
    replace: jest.fn()
  },
  $route: {
    params: {},
    query: {}
  }
}
