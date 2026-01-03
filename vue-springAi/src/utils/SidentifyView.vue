<template>
  <div class="s-canvas">
    <canvas id="s-canvas" :width="props.contentWidth" :height="props.contentHeight"></canvas>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'

// eslint-disable-next-line no-undef
const props = defineProps({
  identifyCode: {
    type: String,
    default: '1234'
  },
  fontSizeMin: {
    type: Number,
    default: 28
  },
  fontSizeMax: {
    type: Number,
    default: 32
  },
  backgroundColorMin: {
    type: Number,
    default: 240
  },
  backgroundColorMax: {
    type: Number,
    default: 250
  },
  colorMin: {
    type: Number,
    default: 50
  },
  colorMax: {
    type: Number,
    default: 120
  },
  lineColorMin: {
    type: Number,
    default: 180
  },
  lineColorMax: {
    type: Number,
    default: 220
  },
  dotColorMin: {
    type: Number,
    default: 150
  },
  dotColorMax: {
    type: Number,
    default: 200
  },
  contentWidth: {
    type: Number,
    default: 112
  },
  contentHeight: {
    type: Number,
    default: 40
  }
})

// 生成一个随机数
const randomNum = (min, max) => {
  return Math.floor(Math.random() * (max - min) + min)
}

// 生成清晰可辨的颜色（深色系）
const getTextColor = (index) => {
  // 使用固定的深色调，确保清晰可读
  const colors = [
    'rgb(66, 66, 180)',   // 深蓝
    'rgb(180, 66, 66)',   // 深红
    'rgb(66, 140, 66)',   // 深绿
    'rgb(140, 66, 140)',  // 深紫
  ]
  return colors[index % colors.length]
}

// 生成一个随机的颜色
const randomColor = (min, max) => {
  let r = randomNum(min, max)
  let g = randomNum(min, max)
  let b = randomNum(min, max)
  return 'rgb(' + r + ',' + g + ',' + b + ')'
}

// 绘制干扰线（减少数量，颜色变浅）
const drawLine = (ctx) => {
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = randomColor(props.lineColorMin, props.lineColorMax)
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight))
    ctx.lineTo(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight))
    ctx.stroke()
  }
}

// 在画布上显示数据（优化：减小旋转角度，字体更清晰）
const drawText = (ctx, txt, i) => {
  ctx.fillStyle = getTextColor(i)
  ctx.font = 'bold ' + randomNum(props.fontSizeMin, props.fontSizeMax) + 'px Arial'
  let x = (i + 1) * (props.contentWidth / (props.identifyCode.length + 1))
  let y = props.contentHeight / 2 + 10
  // 减小旋转角度，让字符更容易辨认
  var deg = randomNum(-15, 15)
  // 修改坐标原点和旋转角度
  ctx.translate(x, y)
  ctx.rotate((deg * Math.PI) / 180)
  ctx.fillText(txt, 0, 0)
  // 恢复坐标原点和旋转角度
  ctx.rotate((-deg * Math.PI) / 180)
  ctx.translate(-x, -y)
}

// 绘制干扰点（减少数量，颜色变浅）
const drawDot = (ctx) => {
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = randomColor(props.dotColorMin, props.dotColorMax)
    ctx.beginPath()
    ctx.arc(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight), 1, 0, 2 * Math.PI)
    ctx.fill()
  }
}

// 画图
const drawPic = () => {
  let canvas = document.getElementById('s-canvas')
  let ctx = canvas.getContext('2d')
  ctx.textBaseline = 'bottom'
  // 绘制背景（浅色背景）
  ctx.fillStyle = '#f5f5f5'
  ctx.fillRect(0, 0, props.contentWidth, props.contentHeight)
  // 绘制文字
  for (let i = 0; i < props.identifyCode.length; i++) {
    drawText(ctx, props.identifyCode[i], i)
  }
  drawLine(ctx)
  drawDot(ctx)
}

// 组件挂载
onMounted(() => {
  drawPic()
})

// 监听
watch(
  () => props.identifyCode,
  () => {
    drawPic()
  }
)
</script>

<style scoped lang="scss">
.s-canvas {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
}
</style>
