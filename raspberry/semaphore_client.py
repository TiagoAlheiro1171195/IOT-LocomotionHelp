import RPi.GPIO as GPIO
import time
import paho.mqtt.client as paho
from paho import mqtt
from gpiozero import LED

# pin setup
PIN_GREEN_LED = 9
PIN_RED_LED = 10

green_led = LED(PIN_GREEN_LED)
red_led = LED(PIN_RED_LED)

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
    
    red_led.off()
    green_led.off()
    if (data == "green") :
    	red_led.blink(on_time=0.2, off_time=0.2, n=5)
    	time.sleep(2)
    	green_led.on()
    elif (data == "red") :
        green_led.blink(on_time=0.2, off_time=0.2, n=5)
        time.sleep(2)
        red_led.on()
   		
  		
client = paho.Client(client_id="semaphore_client", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect
client.on_message = on_message
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
client.username_pw_set("Usertest1", "Usertest1")
client.connect("051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud", 8883)
client.subscribe("semaphore")
client.loop_forever()
