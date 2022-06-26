import RPi.GPIO as GPIO
import time
import paho.mqtt.client as paho
from paho import mqtt
from gpiozero import Button
import pyttsx3

# setup
PIN_BUTTON = 25
TIMEOUT = 30 #seconds
button_active = False
previous_time = 0

button = Button(PIN_BUTTON)

def change_state():
	global button_active
	global previous_time

	if (not button_active) :
		return
	else:
		button_active = False

	answer = time.time() - previous_time < TIMEOUT
		
	client.publish("cross/answer", payload=answer)
	if(answer):
		print("changing signal to green")
		process_audio("changing signal to green")
    
button.when_pressed = change_state

# text to speach setup
engine = pyttsx3.init()
rate = engine.getProperty('rate')
engine.setProperty('rate', rate - 70)
engine.setProperty('voice', 'english')
#voices = engine.getProperty('voices')
#for voice in voices:
#	print(voice.id)

# mqtt setup
def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("Connected to MQTT Broker!")
    else:
        print("Failed to connect, return code %d\n", rc)
      
def on_message(client, userdata, msg):
    data = msg.payload.decode()
    topic = msg.topic
    print(f"Received `{data}` from `{topic}` topic")
    
    if (topic == "device/audio"):
    	process_audio(data)
    elif (topic == "cross/request"):
    	process_request(data)
    
def process_audio(message):
	engine.say(message)
	engine.runAndWait()
	
def process_request(data):
	global button_active
	global previous_time
	
	process_audio(data)
	
	# wait for user input
	button_active = True
	previous_time = time.time()
	button.wait_for_press(timeout=TIMEOUT)
	if(button_active):
		change_state()	

client = paho.Client(client_id="audio_client", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect
client.on_message = on_message
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
client.username_pw_set("Usertest1", "Usertest1")
client.connect("051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud", 8883)
client.subscribe("device/audio")
client.subscribe("cross/request")
client.loop_forever()



