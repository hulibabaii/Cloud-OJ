<template>
  <div v-if="display">
    <TopNavigation active=""/>
    <el-container class="container">
      <el-card style="width: 100%">
        <el-row :gutter="10">
          <el-col :span="6">
            <UserProfile/>
          </el-col>
          <el-col :span="18">
            <Overview/>
          </el-col>
        </el-row>
      </el-card>
      <BottomArea style="margin-top: 35px"/>
    </el-container>
  </div>
</template>

<script>
import TopNavigation from "@/components/common/TopNavigation";
import BottomArea from "@/components/common/BottomArea";
import UserProfile from "@/components/profile/UserProfile";
import Overview from "@/components/profile/Overview";
import {handle401, userInfo} from "@/js/util";

export default {
  name: "Profile",
  beforeMount() {
    if (!this.display) {
      handle401()
    }
    document.title = '个人中心 · Cloud OJ'
  },
  components: {
    TopNavigation,
    BottomArea,
    UserProfile,
    Overview
  },
  data() {
    return {
      display: userInfo() != null
    }
  }
}
</script>

<style scoped>
.container {
  margin-top: 25px;
  padding: 0 10px;
  flex-direction: column;
  align-items: center;
  max-width: 1200px;
}
</style>