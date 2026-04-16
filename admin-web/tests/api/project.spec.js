import { getProjectList, createProject } from '@/api/project'

// Mock axios
jest.mock('@/api/request', () => ({
  __esModule: true,
  default: {
    create: jest.fn(() => ({
      interceptors: {
        request: { use: jest.fn(), eject: jest.fn() },
        response: { use: jest.fn(), eject: jest.fn() }
      },
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn()
    }))
  }
}))

describe('project API', () => {
  it('getProjectList should call GET /api/project/list', async () => {
    const mockResponse = {
      code: 200,
      data: [],
      message: 'success'
    }
    
    // Mock the GET request
    const mockAxios = require('@/api/request').default
    mockAxios.get.mockResolvedValue(mockResponse)
    
    const result = await getProjectList()
    
    expect(mockAxios.get).toHaveBeenCalledWith('/project/list')
    expect(result).toEqual(mockResponse)
  })

  it('createProject should call POST /api/project/create', async () => {
    const mockData = {
      projectName: '测试项目',
      projectCode: 'TEST001'
    }
    
    const mockResponse = {
      code: 200,
      data: true,
      message: 'success'
    }
    
    const mockAxios = require('@/api/request').default
    mockAxios.post.mockResolvedValue(mockResponse)
    
    const result = await createProject(mockData)
    
    expect(mockAxios.post).toHaveBeenCalledWith('/project/create', mockData)
    expect(result).toEqual(mockResponse)
  })
})
