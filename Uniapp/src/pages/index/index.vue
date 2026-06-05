<template>
  <view class="page">
    <view class="banner">
      <swiper :indicator-dots="true" :autoplay="true" class="swiper">
        <swiper-item v-for="i in 3" :key="i">
          <view class="swiper-item">Banner {{ i }}</view>
        </swiper-item>
      </swiper>
    </view>
    <view class="section">
      <view class="section-title">商品分类</view>
      <view class="category-grid">
        <view class="category-item" v-for="c in categories" :key="c.id" @click="goGoodsList(c.id)">
          <text class="category-name">{{ c.name }}</text>
        </view>
      </view>
    </view>
    <view class="section">
      <view class="section-title">推荐商品</view>
      <view class="goods-grid">
        <view class="goods-item" v-for="g in goods" :key="g.id" @click="goDetail(g.id)">
          <image src="/static/logo.png" mode="aspectFill" class="goods-img" />
          <view class="goods-info">
            <text class="goods-name">{{ g.name }}</text>
            <text class="goods-price">¥{{ g.price }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return { categories: [], goods: [] }
  },
  onShow() { this.loadData() },
  methods: {
    loadData() {
      // TODO: API
    },
    goGoodsList(id) { uni.navigateTo({ url: '/pages/goods/list?categoryId=' + id }) },
    goDetail(id) { uni.navigateTo({ url: '/pages/goods/detail?id=' + id }) }
  }
}
</script>

<style scoped>
.page {
  padding-bottom: 20rpx;
}

.banner {
  height: 300rpx;
}

.swiper-item {
  height: 300rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f62fe;
  color: #fff;
  font-size: 36rpx;
}

.section {
  margin: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.category-item {
  padding: 16rpx 24rpx;
  background: #f0f6ff;
  border-radius: 12rpx;
}

.goods-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.goods-item {
  width: calc(50% - 8rpx);
  border-radius: 12rpx;
  overflow: hidden;
  background: #fff;
}

.goods-img {
  width: 100%;
  height: 200rpx;
  background: #e5e5e5;
}

.goods-info {
  padding: 12rpx;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
}

.goods-price {
  font-size: 32rpx;
  color: #f53f2c;
  font-weight: bold;
}
</style>
