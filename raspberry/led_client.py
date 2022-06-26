import RPi.GPIO as GPIO
import time
import paho.mqtt.client as paho
from paho import mqtt
from gpiozero import PWMLED

# pin setup
PIN_LED_1 = 7
PIN_LED_2 = 8
PIN_LED_3 = 11

TIMEOUT = 10 # seconds

# setup leds
leds = {
	"1" : PWMLED(PIN_LED_1),
	"2" : PWMLED(PIN_LED_2),
	"3" : PWMLED(PIN_LED_3)
}

last_times = {
	"1" : 0,
	"2" : 0,
	"3" : 0
}

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
    sensor = topic.split("/")[-1]
    try:
    	leds[sensor].value = float(data)
    	last_times[sensor] = time.time()
    except ValueError:
        leds[sensor].off()
  		
client = paho.Client(client_id="warning_led_client", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect
client.on_message = on_message
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
client.username_pw_set("Usertest1", "Usertest1")
client.connect("051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud", 8883)
client.subscribe("sensor/warning/#")
client.loop_start()

while True:
	for x in last_times.keys():
		if (time.time() - last_times[x] > TIMEOUT):
			last_times[x] = time.time()
			leds[x].off()
