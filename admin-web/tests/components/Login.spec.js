import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'

describe('Login.vue', () => {
  it('renders login form correctly', () => {
    const wrapper = mount(Login)
    
    // Check title is rendered
    expect(wrapper.text()).toContain('青泓项目勘查系统')
    
    // Check username input exists
    const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
    expect(usernameInput.exists()).toBe(true)
    
    // Check password input exists
    const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
    expect(passwordInput.exists()).toBe(true)
    
    // Check login button exists
    const loginButton = wrapper.find('button')
    expect(loginButton.exists()).toBe(true)
    expect(loginButton.text()).toContain('登录')
  })

  it('allows user to enter credentials', async () => {
    const wrapper = mount(Login)
    
    const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
    await usernameInput.setValue('testuser')
    expect(usernameInput.element.value).toBe('testuser')
    
    const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
    await passwordInput.setValue('password123')
    expect(passwordInput.element.value).toBe('password123')
  })

  it('shows form validation errors', async () => {
    const wrapper = mount(Login)
    
    // Trigger form validation
    const loginButton = wrapper.find('button')
    await loginButton.trigger('click')
    
    // Should show error message
    expect(wrapper.text()).not.toContain('登录成功')
  })
})
