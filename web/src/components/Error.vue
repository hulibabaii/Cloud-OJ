<template>
  <div class="error-404">
    <span class="error-code">{{ error.code }}</span>
    <span class="error-text">{{ error.text }}</span>
    <el-button style="margin-top: 20px" size="medium" @click="gotoIndex">
      返回主页
    </el-button>
  </div>
</template>

<script>
export default {
  name: "Error",
  props: {
    error: {
      code: Number,
      text: String
    }
  },
  methods: {
    gotoIndex() {
      window.location.href = './'
    }
  }
}
</script>

<style scoped>
.error-404 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #F56C6C;
}

.error-code {
  font-size: 130pt;
  font-weight: bold;
  opacity: 0.8;
}

.error-text {
  font-size: 20pt;
  opacity: 0.8;
}
</style>