import numpy as np
import cv2
"""
First run Ipcamera app on android phone and enter the given 
ip below
"""
if __name__ == '__main__':

	cap = cv2.VideoCapture("http://172.28.91.35:8080/video?x.mjpeg")

	if not(cap.isOpened()):
		print("Did not open video stream")
		exit()

	while(1):
		ret, frame = cap.read()
		cv2.imshow("image", frame)
		key = cv2.waitKey(33)
		if key == ord('z'):
			break
