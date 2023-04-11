import cv2

def face_finding(img_name, img_path):
    #face_cascade = cv2.CascadeClassifier('lbpcascade_frontalface_improved.xml.xml')
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
    #eye_casecade = cv2.CascadeClassifier('haarcascade_eye.xml')

    realimg = cv2.imread(img_path, cv2.IMREAD_COLOR)
    img = cv2.resize(realimg, (0, 0), fx=0.3, fy=0.3, interpolation=cv2.INTER_AREA)
    cv2.imshow("img", img)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.1, 3)
    print(faces)

    for (x, y, w, h) in faces:
        cropped = img[y: y + h, x: x + w]   # img[y - int(h / 4):y + h + int(h / 4), x - int(w / 4):x + w + int(w / 4)]
        cv2.imwrite(str(img_name), cropped)
        print('image save')
        # cv2.rectangle(img, (x,y), (x+w, y+h), (255,0,0),2)
        # roi_gray = gray[y:y+h, x:x+w]
        # roi_color = img[y:y+h, x:x+w]
        # eyes = eye_casecade.detectMultiScale(roi_gray)
        # for (ex, ey, ew, eh) in eyes:
        #     cv2.rectangle(roi_color, (ex,ey), (ex+ew, ey+eh),(0,255,0),2)
