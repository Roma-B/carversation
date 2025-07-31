<script setup>
import { ref, onMounted } from 'vue'
import { GoogleMap, Marker, InfoWindow } from 'vue3-google-map'

// === Reactive References ===
const vin = ref('')
const latitude = ref('')
const longitude = ref('')
const isFormSubmitted = ref(false)

const center = ref(null)
const selectedCarIndex = ref(null)
const chatInput = ref('')

const isRecordingText = ref(false)
const isRecordingAudio = ref(false)
const mediaRecorder = ref(null)
const audioChunks = []

const chatMessages = ref([])         // Stores all chat messages (text or audio)
const lastVoiceBlob = ref(null)      // Stores the last recorded voice message

const showRequestPopup = ref(false)
const requestCarIndex = ref(null)

const incomingChatRequest = ref(null)
const showIncomingRequestPopup = ref(false)

const ws = ref(null)
const isWebSocketConnected = ref(false)
const currentCar = ref()
const nearByCars = ref([])

const submitForm = async () => {
  if (!vin.value || !latitude.value || !longitude.value) {
    alert('Please fill all fields')
    return
  }

  try {
    const lat = parseFloat(latitude.value)
    const lng = parseFloat(longitude.value)

    const response = await fetch(`http://localhost:8080/users/nearby?lat=${lat}&lng=${lng}&vin=${vin.value}&radius=3000`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) throw new Error('Failed to fetch')

    const result = await response.json()

    currentCar.value = {
        vin: vin.value,
        position: { lat, lng },
        icon: {
          url: 'https://img.icons8.com/color/48/000000/car--v1.png',
          scaledSize: { width: 40, height: 40 }
        },
        carName: result.myName
    }

    // Pushing current car details
    nearByCars.value.push(currentCar.value)

    // Pushing near by car details
    result.nearByUsers.forEach(element => {

      nearByCars.value.push({
        vin: element.vin,
        position: { lat: element.lat, lng: element.lng },
        icon: {
          url: 'https://img.icons8.com/color/48/000000/car--v1.png',
          scaledSize: { width: 40, height: 40 }
        },
        carName: element.name
      })

    });

    isFormSubmitted.value = true
    console.log("Result: ", result)
    connectWebSocket()
  } catch (err) {
    console.error(err)
    alert('API error. Check console.')
  }
}

const updateCar = (element) => {
  const existingIndex = nearByCars.value.findIndex(car => car.vin === element.vin)

  const updatedCar = {
    vin: element.vin,
    position: { lat: element.lat, lng: element.lng },
    icon: {
      url: 'https://img.icons8.com/color/48/000000/car--v1.png',
      scaledSize: { width: 40, height: 40 }
    },
    carName: element.name
  }

  if (existingIndex !== -1) {
    // Update existing car
    nearByCars.value[existingIndex] = updatedCar
  } else {
    // Add new car
    nearByCars.value.push(updatedCar)
  }
}

const connectWebSocket = () => {
  ws.value = new WebSocket(`ws://localhost:8080/chatRequest?vin=${vin.value}`)

  ws.value.onopen = () => {
    isWebSocketConnected.value = true
    console.log('✅ WebSocket connected')
  }

  ws.value.onmessage = (event) => {
    try {
      const messageObj = JSON.parse(event.data)
      console.log('📩 Message received:', messageObj)

      if(messageObj.messageType === "NEARBY_USERS") {
        messageObj.nearByUsers.nearByUsers.forEach(element => {
          updateCar(element)      
        });
      }

      if(messageObj.messageType === "CHAT_REQUEST_RECEIVED" && messageObj.requestTo === vin.value) {
        incomingChatRequest.value = messageObj
        showIncomingRequestPopup.value = true
      }

      if(messageObj.messageType === "CHAT_REQUEST_ACCEPTED") {
        console.log("Req from: " + messageObj.requestFrom)
        const indexToOpenChatBox = nearByCars.value.findIndex(car => car.vin === messageObj.requestFrom)
        console.log("Index to open chat box: " + indexToOpenChatBox)
        openChatBox(indexToOpenChatBox)
      }

      if(messageObj.messageType === "CHAT_MESSAGE") {
        const index = nearByCars.value.findIndex(car => car.vin === messageObj.senderVin)
        
        chatMessages.value.push({
          type: 'text',
          content: messageObj.message,
          name: nearByCars.value[index].carName
        })

      }
    } catch (err) {
      console.error('❌ Failed to parse WebSocket message:', err)
    }
  }

  ws.value.onclose = () => {
    isWebSocketConnected.value = false
    console.log('❌ WebSocket disconnected')
  }

  ws.value.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

const acceptChatRequest = () => {
  if (ws.value && isWebSocketConnected.value && incomingChatRequest.value) {
    const responsePayload = {
      messageType: "CHAT_REQUEST_RESPONSE",
      status: "ACCEPTED",
      vin: incomingChatRequest.value.requestFrom
    }

    ws.value.send(JSON.stringify(responsePayload))
    showIncomingRequestPopup.value = false

    const indexToOpenChatBox = nearByCars.value.findIndex(car => car.vin === incomingChatRequest.value.requestFrom)
    openChatBox(indexToOpenChatBox)
    incomingChatRequest.value = null
  }
}

const rejectChatRequest = () => {
  if (ws.value && isWebSocketConnected.value && incomingChatRequest.value) {
    const responsePayload = {
      messageType: "CHAT_REQUEST_RESPONSE",
      status: "REJECTED",
      vin: incomingChatRequest.value.requestFrom
    }
    ws.value.send(JSON.stringify(responsePayload))
    showIncomingRequestPopup.value = false
    incomingChatRequest.value = null
  }
}

// === Get User Location ===
onMounted(() => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        center.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        }
      },
      (error) => {
        console.error('Geolocation error:', error)
        center.value = { lat: 28.6139, lng: 77.2090 } // Default to Delhi
      }
    )
  }
})

// === Open Request Chat Box for Clicked Marker ===
const openRequestChatBox = (index) => {

  if (nearByCars.value[index].carName === currentCar.value.carName) return // prevent self

  requestCarIndex.value = index
  showRequestPopup.value = true
}

const openChatBox = (index) => {
  selectedCarIndex.value = index
}

const confirmChatRequest = () => {
  const chatRequestPayload = {
    vin: nearByCars.value[requestCarIndex.value].vin,
    "messageType": "CHAT_REQUEST"
  }

  ws.value.send(JSON.stringify(chatRequestPayload))
  showRequestPopup.value = false
  requestCarIndex.value = null
}

const cancelChatRequest = () => {
  showRequestPopup.value = false
  requestCarIndex.value = null
}

// === Start Speech-to-Text ===
const startSpeechToText = () => {
  if (!('webkitSpeechRecognition' in window)) {
    alert('Speech recognition not supported in this browser.')
    return
  }

  const recognition = new webkitSpeechRecognition()
  recognition.lang = 'en-US'
  recognition.continuous = false
  recognition.interimResults = false
  isRecordingText.value = true

  recognition.start()

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript
    chatInput.value = transcript
    isRecordingText.value = false
  }

  recognition.onerror = () => {
    alert('Speech recognition error.')
    isRecordingText.value = false
  }
}

// === Start Audio Recording ===
const startVoiceRecording = async () => {
  if (!navigator.mediaDevices) {
    alert('Audio recording not supported.')
    return
  }

  const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
  mediaRecorder.value = new MediaRecorder(stream)

  mediaRecorder.value.ondataavailable = (e) => {
    audioChunks.push(e.data)
  }

  mediaRecorder.value.onstop = () => {
    const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
    lastVoiceBlob.value = audioBlob // Store for later use in chat
    audioChunks.length = 0
    isRecordingAudio.value = false
  }

  mediaRecorder.value.start()
  isRecordingAudio.value = true
  setTimeout(() => {
    mediaRecorder.value.stop()
  }, 10000) // Record 5 sec
}

const stopVoiceRecording = () => {
  if(isRecordingAudio.value) {
    mediaRecorder.value.stop()
  }
}

// === Send Audio to Backend (Mock Function) ===
const sendAudioToBackend = (audioBlob) => {
  const formData = new FormData()
  formData.append('audio', audioBlob, 'voiceMessage.webm')
  console.log('Sending audio blob to backend...')

  // TODO: Replace with actual API call
  // fetch('/api/send-voice', { method: 'POST', body: formData })
}

const sendChatMessage = () => {
  if (chatInput.value.trim()) {

    if (ws.value && isWebSocketConnected.value) {

      console.log("Send VIN: " + nearByCars.value[selectedCarIndex.value].vin)

      const sendMsgPayload = {
        vin: nearByCars.value[selectedCarIndex.value].vin,
        messageType: "CHAT_MESSAGE",
        message: chatInput.value
      }

      console.log("Send Msg Payload: " + JSON.stringify(sendMsgPayload))

      ws.value.send(JSON.stringify(sendMsgPayload))

      chatMessages.value.push({ type: 'text', content: chatInput.value, name: 'You' })
      chatInput.value = ''
    }
  } else if (lastVoiceBlob.value) {
    const url = URL.createObjectURL(lastVoiceBlob.value)
    chatMessages.value.push({ type: 'audio', content: url })
    lastVoiceBlob.value = null
  }
}

const getNameByVinId = (vin) => {
  const index = nearByCars.value.findIndex(car => car.vin === vin)

  return nearByCars.value[index].carName
}
</script>

<template>
  <div v-if="!isFormSubmitted" class="overlay">
    <div class="form-box">
      <h2>Enter VIN & Coordinates</h2>
      <input v-model="vin" placeholder="Enter VIN" />
      <input v-model="latitude" placeholder="Enter Latitude" />
      <input v-model="longitude" placeholder="Enter Longitude" />
      <button @click="submitForm">Submit</button>
    </div>
  </div>

  <GoogleMap
    v-else
    api-key="AIzaSyC4Dy8OfX8R_t7mosrnp1HT86xCMdxoGOA"
    style="width: 100%; height: 700px"
    :center="center"
    :zoom="18"
  >
    <Marker
      v-for="(car, index) in nearByCars"
      :key="index"
      :options="car"
      @click="openRequestChatBox(index)"
    />

    <InfoWindow
      v-for="(car, index) in nearByCars"
      v-if="selectedCarIndex !== index"
      :key="'label-' + index"
      :options="{
        content: `<div style='display:flex;align-items:center;font-weight:bold;'>${car.carName === vin ? 'You' : car.carName}</div>`,
        position: car.position,
        pixelOffset: { width: 0, height: -40 }
      }"
    />
  </GoogleMap>

  <div v-if="showRequestPopup" class="popup-overlay">
    <div class="popup-box">
      <p>Send chat request to <strong>{{ nearByCars[requestCarIndex]?.carName }}</strong>?</p>
      <div class="popup-buttons">
        <button @click="confirmChatRequest">Yes</button>
        <button @click="cancelChatRequest">No</button>
      </div>
    </div>
  </div>

  <div v-if="showIncomingRequestPopup" class="popup-overlay">
    <div class="popup-box">
      <p>
        <strong>{{ getNameByVinId(incomingChatRequest?.requestFrom) }}</strong> wants to chat with you.
      </p>
      <div class="popup-buttons">
        <button @click="acceptChatRequest">Accept</button>
        <button @click="rejectChatRequest">Reject</button>
      </div>
    </div>
  </div>

  <div
    v-if="selectedCarIndex !== null"
    class="chat-box"
  >
    <div class="chat-header">
      Chat with {{ nearByCars[selectedCarIndex].carName }}
      <button @click="selectedCarIndex = null">✖</button>
    </div>

    <div class="chat-body">
      <div v-for="(msg, i) in chatMessages" :key="i">
        <p v-if="msg.type === 'text'"><strong>{{msg.name}}:</strong> {{ msg.content }}</p>
        <p v-else>
          <strong>{{msg.name}} (voice):</strong>
          <audio :src="msg.content" controls></audio>
        </p>
      </div>
    </div>

    <div class="chat-input">
      <input type="text" v-model="chatInput" placeholder="Type a message..." />
      <!-- Specch To Text Button -->
      <button
        :class="{ 'recording-active': isRecordingText }"
        @click="startSpeechToText"
      >
        <span v-if="isRecordingText"> 🔴</span>
        <span v-else>🗣️ STT</span>
      </button>

      <!-- Voice Button -->
      <button
        :class="{ 'recording-active': isRecordingAudio }"
        @click="isRecordingAudio ? stopVoiceRecording() : startVoiceRecording()"
      >
        <span v-if="isRecordingAudio" @click="stopVoiceRecording"> 🔴</span>
        <span v-else>🎙️ Voice</span>
      </button>

      <button @click="sendChatMessage">Send</button>
    </div>
  </div>
</template>

<style>
.chat-box {
  position: absolute;
  bottom: 20px;
  left: 30px;
  width: 400px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 999;
  font-family: Arial, sans-serif;
}

.chat-header {
  background: #1976d2;
  color: white;
  padding: 10px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.chat-body {
  padding: 10px;
  height: 150px;
  overflow-y: auto;
  background: #f9f9f9;
}

.chat-input {
  display: flex;
  padding: 10px;
  gap: 5px;
  border-top: 1px solid #eee;
}

.chat-input input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.chat-input button {
  padding: 8px 10px;
  background: #1976d2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button.recording-active {
  border: 2px solid red;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0px red; }
  50% { box-shadow: 0 0 10px red; }
  100% { box-shadow: 0 0 0px red; }
}

.custom-label {
  position: absolute;
  top: -50px;
  left: -50%;
  transform: translateX(-50%);
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

.car-message {
  margin-top: 4px;
  background: #ffeeba;
  border: 1px solid #f5c06b;
  color: #856404;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-box {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 300px;
}

input {
  padding: 0.5rem;
  font-size: 1rem;
}

button {
  padding: 0.7rem;
  background: #2a9d8f;
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
}

.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  background-color: rgba(0,0,0,0.4);
  width: 100%;
  height: 100%;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.popup-box {
  background-color: white;
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
  text-align: center;
}

.popup-buttons {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.popup-buttons button {
  padding: 0.6rem 1rem;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background-color: #1976d2;
  color: white;
}

</style>
