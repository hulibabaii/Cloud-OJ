<template>
  <div style="margin: 25px 45px">
    <el-form ref="loginForm"
             :rules="loginRules"
             :model="loginForm">
      <el-form-item prop="username">
        <el-input class="login-input" auto-complete="off"
                  prefix-icon="el-icon-postcard"
                  placeholder="ID"
                  v-model="loginForm.username">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input class="login-input" type="password" auto-complete="new-password"
                  prefix-icon="el-icon-lock"
                  placeholder="密码"
                  v-model="loginForm.password">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="login-button" type="primary" round
                   :disabled="disableLogin"
                   @click="onLogin">登录
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {apiPath, copyObject} from "@/js/util";

export default {
  name: "LoginTab",
  data() {
    return {
      disableLogin: false,
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          {required: true, message: '请输入用户名', trigger: 'blur'}
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    onLogin() {
      this.disableLogin = true
      this.$refs['loginForm'].validate((valid) => {
        if (valid) {
          let data = copyObject(this.loginForm)
          data.password = this.$md5(data.password)
          this.$axios({
            url: apiPath.login,
            method: 'post',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            data: this.qs.stringify(data)
          }).then((res) => {
            // token 保存到会话存储
            sessionStorage.setItem('cloud_oj_token', JSON.stringify(res.data))
            window.location.href = '../'
          }).catch((error) => {
            this.$notify.error({
              title: '登录失败',
              message: `${error.response.status}`
            })
          }).finally(() => {
            this.disableLogin = false
          })
        } else {
          this.disableLogin = false
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-input {
  width: 320px;
}

.login-button {
  width: 100%;
}
</style>