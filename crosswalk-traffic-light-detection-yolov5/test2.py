from turtle import update
import torch
import numpy as np
from utils.datasets import letterbox
from utils.general import non_max_suppression, scale_coords
from utils.plots import Annotator
import pickle
from threading import Thread
from queue import Queue
import time
import cv2 
import multiprocessing
# import imutils

class FileVideoStream:
	def __init__(self, path, queueSize=100000):
		# initialize the file video stream along with the boolean
		# used to indicate if the thread should be stopped or not
		self.stream = cv2.VideoCapture('data/sample.mp4')
		self.stopped = False
		# initialize the queue used to store frames read from
		# the video file
		self.Q = Queue(maxsize=queueSize)

	def start(self):
		# start a thread to read frames from the file video stream
		# t = Thread(target=self.update, args=())
		# t.daemon = True
		t = multiprocessing.Process(target=self.update)
		t.start()
		return self

	# def update(self):
	# 	# keep looping infinitely
	# 	MODEL_PATH = 'runs/train/exp4/weights/best.pt'

	# 	img_size = 640
	# 	conf_thres = 0.5  # confidence threshold
	# 	iou_thres = 0.45  # NMS IOU threshold
	# 	max_det = 500  # maximum detections per image
	# 	classes = None  # filter by class
	# 	agnostic_nms = False  # class-agnostic NMS

	# 	device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

	# 	ckpt = torch.load(MODEL_PATH, map_location=device)
	# 	model = ckpt['ema' if ckpt.get('ema') else 'model'].float().fuse().eval()
	# 	class_names = ['gray', 'red', 'green'] # model.names
	# 	stride = int(model.stride.max())
	# 	colors = ((50, 50, 50), (0, 0, 255), (0, 255, 0)) # (gray, red, green)

	# 	while True:
	# 		# if the thread indicator variable is set, stop the
	# 		# thread
	# 		if self.stopped:
	# 			return
	# 		# otherwise, ensure the queue has room in it
	# 		if not self.Q.full():
	# 			# read the next frame from the file
	# 			grabbed, img = self.stream.read()
	# 			# if the `grabbed` boolean is `False`, then we have
	# 			# reached the end of the video file
	# 			if not grabbed:
	# 				self.stop()
	# 				return

	# 			# preprocess
	# 			img_input = letterbox(img, img_size, stride=stride)[0]
	# 			img_input = img_input.transpose((2, 0, 1))[::-1]
	# 			img_input = np.ascontiguousarray(img_input)
	# 			img_input = torch.from_numpy(img_input).to(device)
	# 			img_input = img_input.float()
	# 			img_input /= 255.
	# 			img_input = img_input.unsqueeze(0)

	# 			# inference
	# 			pred = model(img_input, augment=False, visualize=False)[0]

	# 			# postprocess
	# 			pred = non_max_suppression(pred, conf_thres, iou_thres, classes, agnostic_nms, max_det=max_det)[0]

	# 			pred = pred.cpu().numpy()

	# 			pred[:, :4] = scale_coords(img_input.shape[2:], pred[:, :4], img.shape).round()

	# 			annotator = Annotator(img.copy(), line_width=3, example=str(class_names), font='data/malgun.ttf')

	# 			for p in pred:
	# 				class_name = class_names[int(p[5])]

	# 				x1, y1, x2, y2 = p[:4]

	# 				annotator.box_label([x1, y1, x2, y2], '%s %d' % (class_name, float(p[4]) * 100), color=colors[int(p[5])])

	# 			result_img = annotator.result()
	
	# 			# add the frame to the queue
	# 			self.Q.put(result_img)
	def update(self):
		# keep looping infinitely
		while True:
			# if the thread indicator variable is set, stop the
			# thread
			if self.stopped:
				return
			# otherwise, ensure the queue has room in it
			if not self.Q.full():
				# read the next frame from the file
				(grabbed, frame) = self.stream.read()
				# if the `grabbed` boolean is `False`, then we have
				# reached the end of the video file
				if not grabbed:
					self.stop()
					return
				# add the frame to the queue
				self.Q.put(frame)
	def read(self):
		# return next frame in the queue
		return self.Q.get()

	def more(self):
		# return True if there are still frames in the queue
		return self.Q.qsize() > 0

	def stop(self):
		# indicate that the thread should be stopped
		self.stopped = True




# MODEL_PATH = 'runs/train/exp4/weights/best.pt'

# img_size = 640
# conf_thres = 0.5  # confidence threshold
# iou_thres = 0.45  # NMS IOU threshold
# max_det = 500  # maximum detections per image
# classes = None  # filter by class
# agnostic_nms = False  # class-agnostic NMS

# device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

# ckpt = torch.load(MODEL_PATH, map_location=device)
# model = ckpt['ema' if ckpt.get('ema') else 'model'].float().fuse().eval()
# class_names = ['gray', 'red', 'green'] # model.names
# stride = int(model.stride.max())
# colors = ((50, 50, 50), (0, 0, 255), (0, 255, 0)) # (gray, red, green)

#cap = cv2.VideoCapture('http://192.168.0.108:8080/video/mjpeg')

cap = FileVideoStream('data/sample.mp4').start()
time.sleep(1.0)

# fourcc = cv2.VideoWriter_fourcc('m', 'p', '4', 'v')
#out = cv2.VideoWriter('data/output.mp4', fourcc, cap.get(cv2.CAP_PROP_FPS), (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))))
#out = cv2.VideoWriter('data/output.mp4', fourcc, cap.stream.get(cv2.CAP_PROP_FPS), (int(cap.stream.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.stream.get(cv2.CAP_PROP_FRAME_HEIGHT))))

#while cap.isOpened():
while cap.more():
    # ret, img = cap.read()
    img = cap.read()
    
    # frame = imutils.resize(img, width=450)
    # frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # frame = np.dstack([frame, frame, frame])
	# display the size of the queue on the frame
    cv2.putText(img, "Queue Size: {}".format(cap.Q.qsize()),
		(10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)	

	# show the frame and update the FPS counter
    cv2.imshow('result', img)
    #out.write(img)
    # if cv2.waitKey(1) == ord('q'):
    #   break
    cv2.waitKey(1)
    cap.update()

    # cv2.imshow('result', result_img)
    # #out.write(result_img)
    # if cv2.waitKey(1) == ord('q'):
    #     break

cv2.destroyAllWindows()
cap.stop()
