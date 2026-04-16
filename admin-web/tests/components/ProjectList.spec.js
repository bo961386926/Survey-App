import { mount } from '@vue/test-utils'
import ProjectList from '@/views/project/ProjectList.vue'

describe('ProjectList.vue', () => {
  it('renders project list header', () => {
    const wrapper = mount(ProjectList)
    
    // Check header text
    expect(wrapper.text()).toContain('项目列表')
    expect(wrapper.text()).toContain('新建项目')
  })

  it('shows add project button', () => {
    const wrapper = mount(ProjectList)
    
    const addButton = wrapper.find('button')
    expect(addButton.exists()).toBe(true)
    expect(addButton.text()).toContain('新建项目')
  })

  it('renders empty state when no projects', () => {
    const wrapper = mount(ProjectList)
    
    // Check empty table
    const table = wrapper.find('.el-table')
    expect(table.exists()).toBe(true)
  })
})
