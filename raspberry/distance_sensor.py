import RPi.GPIO as GPIO
import time
import paho.mqtt.client as paho
from paho import mqtt
from gpiozero import Button

# pin setup
PIN_TRIGGER_1 = 26
PIN_ECHO_1 = 20

PIN_TRIGGER_2 = 19
PIN_ECHO_2 = 16

PIN_TRIGGER_3 = 6
PIN_ECHO_3 = 12

PIN_BUTTON = 21

# Sonar Placements
#   2
# 1   3

MAX_DISTANCE = 400 # cm
SENSOR_INTERVAL = .5 # second
previous_time = 0
active = False
sensor_active = False

GPIO.setmode(GPIO.BCM) # GPIO numbers 

GPIO.setup(PIN_TRIGGER_1, GPIO.OUT)
GPIO.setup(PIN_ECHO_1, GPIO.IN)

GPIO.setup(PIN_TRIGGER_2, GPIO.OUT)
GPIO.setup(PIN_ECHO_2, GPIO.IN)

GPIO.setup(PIN_TRIGGER_3, GPIO.OUT)
GPIO.setup(PIN_ECHO_3, GPIO.IN)

button = Button(PIN_BUTTON)

def change_state():
    global active
    active = not active
    
    if (active):
    	print("Started")
    else :
    	print("Stopped")
    
button.when_pressed = change_state

# mqtt setup
def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("Connected to MQTT Broker!")
    else:
        print("Failed to connect, return code %d\n", rc)

client = paho.Client(client_id="ultrasonic_sensor_client", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
client.username_pw_set("Usertest1", "Usertest1")
client.connect("051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud", 8883)
client.loop_start()

def loop():
    current_time = time.time() 
    global active
    global sensor_active
    global previous_time
    global SENSOR_INTERVAL
    
    if current_time - previous_time >= SENSOR_INTERVAL:
        previous_time = current_time
        sensor_active = True
    else :
        sensor_active = False    
 
    if active and sensor_active:
        read_send_distance('1', PIN_TRIGGER_1, PIN_ECHO_1)
        time.sleep(0.25)
        read_send_distance('2', PIN_TRIGGER_2, PIN_ECHO_2)
        time.sleep(0.25)
        read_send_distance('3', PIN_TRIGGER_3, PIN_ECHO_3)
        print("---------------------------------------")
  


def read_send_distance(id, trigger, echo):
    global MAX_DISTANCE

    GPIO.output(trigger, GPIO.LOW)

    time.sleep(0.00001)

    GPIO.output(trigger, GPIO.HIGH)

    time.sleep(0.0001)

    GPIO.output(trigger, GPIO.LOW)

    while GPIO.input(echo) == 0:
        pulse_start_time = time.time()
    while GPIO.input(echo) == 1:
        pulse_end_time = time.time()

    pulse_duration = pulse_end_time - pulse_start_time
    distance = round(pulse_duration * 17150, 2)

    
    if distance > MAX_DISTANCE or distance < 1:
        print("Error calculating")
        return

    print("--------------------------------------")
    print(f"Sensor {id} measured distance {distance}")
    
    client.publish(f"sensor/distance/{id}", payload=distance, qos=0, retain=False)


print("Press Button to begin")
button.wait_for_press()
while True:
    loop()
