<script setup>
import { onMounted, ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

// Generate random confetti pieces
const confettiPieces = ref([])

const generateConfetti = () => {
  confettiPieces.value = []
  for (let i = 0; i < 80; i++) {
    confettiPieces.value.push({
      id: i,
      left: Math.random() * 100,
      delay: Math.random() * 2,
      duration: 3 + Math.random() * 3,
      size: 4 + Math.random() * 8,
      color: getRandomColor(),
      rotation: Math.random() * 360,
      drift: (Math.random() - 0.5) * 100
    })
  }
}

const getRandomColor = () => {
  const colors = [
    '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', 
    '#feca57', '#ff9ff3', '#54a0ff', '#5f27cd',
    '#00d2d3', '#ff9f43', '#ff6348', '#6c5ce7'
  ]
  return colors[Math.floor(Math.random() * colors.length)]
}

// Play celebration sound effect
const playCelebrationSound = () => {
  // Create a simple celebration beep sound using Web Audio API
  try {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    oscillator.frequency.setValueAtTime(523.25, audioContext.currentTime) // C5
    oscillator.frequency.setValueAtTime(659.25, audioContext.currentTime + 0.1) // E5
    oscillator.frequency.setValueAtTime(783.99, audioContext.currentTime + 0.2) // G5
    
    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5)
    
    oscillator.start(audioContext.currentTime)
    oscillator.stop(audioContext.currentTime + 0.5)
  } catch (error) {
    console.log('Audio not available:', error)
  }
}

onMounted(() => {
  generateConfetti()
})

// Watch for show prop changes to trigger effects
watch(() => props.show, (newValue) => {
  if (newValue) {
    generateConfetti()
    playCelebrationSound()
  }
})
</script>

<template>
  <div v-if="show" class="confetti-container">
    <!-- Confetti pieces -->
    <div
      v-for="piece in confettiPieces"
      :key="piece.id"
      class="confetti-piece"
      :style="{
        left: piece.left + '%',
        width: piece.size + 'px',
        height: piece.size + 'px',
        backgroundColor: piece.color,
        animationDelay: piece.delay + 's',
        animationDuration: piece.duration + 's',
        '--drift': piece.drift + 'px'
      }"
    ></div>
    
    <!-- Celebration text overlay -->
    <div class="celebration-overlay">
      <div class="celebration-text">
        <h1 class="celebration-title">🎉 CHÚC MỪNG! 🎉</h1>
        <p class="celebration-subtitle">Bạn đã hoàn thành mục tiêu!</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.confetti-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
  overflow: hidden;
}

.confetti-piece {
  position: absolute;
  width: 10px;
  height: 10px;
  top: -10px;
  animation: confetti-fall linear infinite;
  border-radius: 2px;
}

.confetti-piece:nth-child(odd) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.confetti-piece:nth-child(3n) {
  width: 6px;
  height: 12px;
  border-radius: 0;
}

.confetti-piece:nth-child(4n) {
  width: 12px;
  height: 6px;
  border-radius: 0;
}

@keyframes confetti-fall {
  0% {
    transform: translateY(-100vh) translateX(0) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: translateY(50vh) translateX(var(--drift)) rotate(360deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) translateX(calc(var(--drift) * 1.5)) rotate(720deg);
    opacity: 0;
  }
}

.celebration-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: celebration-fade-in 0.5s ease-out;
}

.celebration-text {
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem 3rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  animation: celebration-bounce 0.6s ease-out;
}

.celebration-title {
  font-size: 3rem;
  font-weight: bold;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #feca57);
  background-size: 400% 400%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient-shift 2s ease infinite;
  margin: 0 0 1rem 0;
  text-shadow: 0 0 30px rgba(255, 107, 107, 0.5);
}

.celebration-subtitle {
  font-size: 1.5rem;
  color: #333;
  margin: 0;
  font-weight: 500;
}

@keyframes celebration-fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes celebration-bounce {
  0% {
    transform: scale(0.3) translateY(-50px);
    opacity: 0;
  }
  50% {
    transform: scale(1.05) translateY(0);
  }
  70% {
    transform: scale(0.95) translateY(0);
  }
  100% {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

@keyframes gradient-shift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* Mobile responsive */
@media (max-width: 768px) {
  .celebration-title {
    font-size: 2rem;
  }
  
  .celebration-subtitle {
    font-size: 1.2rem;
  }
  
  .celebration-text {
    padding: 1.5rem 2rem;
    margin: 0 1rem;
  }
}
</style> 