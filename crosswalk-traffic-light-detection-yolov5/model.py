from soupsieve import match
from sqlalchemy import null
import torch
import numpy as np
import cv2
from utils.datasets import letterbox
from utils.general import non_max_suppression, scale_coords
from utils.plots import Annotator
import pickle
import time
import paho.mqtt.client as paho
from paho import mqtt


# setting callbacks for different events to see if it works, print the message etc.
def on_connect(client, userdata, flags, rc, properties=None):
    print("CONNACK received with code %s." % rc)

# with this callback you can see if your publish was successful
def on_publish(client, userdata, mid, properties=None):
    print("mid: " + str(mid))

# print which topic was subscribed to
def on_subscribe(client, userdata, mid, granted_qos, properties=None):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

# print message, useful for checking if it was successful
def on_message(client, userdata, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))

# using MQTT version 5 here, for 3.1.1: MQTTv311, 3.1: MQTTv31
# userdata is user defined data of any type, updated by user_data_set()
# client_id is the given name of the client
client = paho.Client(client_id="", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect

# enable TLS for secure connection
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
# set username and password
client.username_pw_set("Usertest1", "Usertest1")
# connect to HiveMQ Cloud on port 8883 (default for MQTT)
client.connect("051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud", 8883)

# setting callbacks, use separate functions like above for better visibility
#client.on_subscribe = on_subscribe
client.on_message = on_message
client.on_publish = on_publish
client.loop_start()

# subscribe to all topics of encyclopedia by using the wildcard "#"
# client.subscribe("encyclopedia/#", qos=1)

# a single publish, this can also be done in loops, etc.
# client.publish("encyclopedia/temperature", payload="hot", qos=1)

# loop_forever for simplicity, here you need to stop the loop manually
# you can also use loop_start and loop_stop
#client.loop_forever()

MODEL_PATH = 'runs/train/exp4/weights/best.pt'

img_size = 640
conf_thres = 0.5  # confidence threshold
iou_thres = 0.45  # NMS IOU threshold
max_det = 2  # maximum detections per image
classes = None  # filter by class
agnostic_nms = False  # class-agnostic NMS

device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

ckpt = torch.load(MODEL_PATH, map_location=device)
model = ckpt['ema' if ckpt.get('ema') else 'model'].float().fuse().eval()
class_names = ['gray', 'red', 'green'] # model.names
stride = int(model.stride.max())
colors = ((50, 50, 50), (0, 0, 255), (0, 255, 0)) # (gray, red, green)

#cap = cv2.VideoCapture('data/sample.mp4')
cap = cv2.VideoCapture('http://192.168.255.57:81/stream')

fourcc = cv2.VideoWriter_fourcc('m', 'p', '4', 'v')
out = cv2.VideoWriter('data/output.mp4', fourcc, cap.get(cv2.CAP_PROP_FPS), (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))))
#out = cv2.VideoWriter('data/output.mp4', fourcc, cap.get(30), (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))))

count = 0

while cap.isOpened():
   
    #client.loop_start()
    
    
    ret, img = cap.read()
    if not ret:
        break

    # preprocess
    img_input = letterbox(img, img_size, stride=stride)[0]
    img_input = img_input.transpose((2, 0, 1))[::-1]
    img_input = np.ascontiguousarray(img_input)
    img_input = torch.from_numpy(img_input).to(device)
    img_input = img_input.float()
    img_input /= 255.
    img_input = img_input.unsqueeze(0)

    # inference
    pred = model(img_input, augment=False, visualize=False)[0]

    # postprocess
    pred = non_max_suppression(pred, conf_thres, iou_thres, classes, agnostic_nms, max_det=max_det)[0]

    pred = pred.cpu().numpy()

    pred[:, :4] = scale_coords(img_input.shape[2:], pred[:, :4], img.shape).round()

    annotator = Annotator(img.copy(), line_width=3, example=str(class_names), font='data/malgun.ttf')

    detected_red = null
    detected_green = null
    detected_road = null

    for p in pred:
        class_name = class_names[int(p[5])]

        x1, y1, x2, y2 = p[:4]

        annotator.box_label([x1, y1, x2, y2], '%s %d' % (class_name, float(p[4]) * 100), color=colors[int(p[5])])

        print("detected: " + class_name)
        
        client.publish("camera/detection", payload=class_name, qos=0)

    result_img = annotator.result()

    cv2.imshow('result', result_img)
    
    out.write(result_img)
    
    if cv2.waitKey(1) == ord('q'):
        break

    count += 30 # i.e. at 30 fps, this advances one second
    cap.set(cv2.CAP_PROP_POS_FRAMES, count)

cap.release()
out.release()
