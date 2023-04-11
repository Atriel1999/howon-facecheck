import os
import shutil
import cv2
import numpy
from PIL.ExifTags import *
from PIL import Image


def jpg_copy(source, destination):

    file_list = os.listdir(source)

    for file in file_list:
        if(file.endswith(".jpg")):
            print("file name " + file +" move to "+destination)
            shutil.copy(source+'/'+file, destination+'/'+file)

def jpg_move(source, destination):

    file_list = os.listdir(source)

    for file in file_list:
        if(file.endswith(".jpg")):
            print("file name " + file +" move to "+destination)
            shutil.move(source+'/'+file, destination+'/'+file)

def jpg_remove(source):

    file_list = os.listdir(source)

    for file in file_list:
        if(file.endswith(".jpg")):
            os.unlink(source + '/' + file)
            print("img removed " + source + "/" + file)

def createFoler(dir):
    try:
        if not os.path.exists(dir):
            os.makedirs(dir)
    except OSError:
        print('Error: Creating directory.' + dir)

def img_rotation(source, degree):
    file_list = os.listdir(source)

    for file in file_list:
        if (file.endswith(".jpg")):
            img = Image.open(os.path.join(source, file))
            out = img.rotate(degree, expand=True)  # save(file)
            out.save(source+ '/' + file)
            img.close()

            # img = cv2.imread(source + '/' + file, cv2.IMREAD_COLOR)
            # # h, w = img.shape[:2]
            # # x, y = w//2, h//2
            # # rotation = cv2.getRotationMatrix2D((x, y), 90, 1)
            # # img270 = cv2.warpAffine(img, rotation, (w, h))
            #
            # img270 = cv2.rotate(img, cv2.ROTATE_90_COUNTERCLOCKWISE)
            # print(source + '/' + file +" has been rotated")
            #
            # cv2.imshow("rotation_image", img270)
            #
            # ## os.unlink(source + '/' + file)
            # cv2.imwrite("rotate"+s1+".jpg", img270)



