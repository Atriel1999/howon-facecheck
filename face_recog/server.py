import socket
import os
from _thread import *
import time

import PIL
import cv2

import face_compare as face
from opencv_findface import face_finding
from file_move import *
from mysql_connector import *

result = ''
idx = 0
socket = socket
#test
#서버프로토콜 헤더(1바이트,문자전송0x01,4~7 출석이미지전송0x02~3 등록이미지전송0x03(아직안만듬)), 데이터길이 4bytes, 데이터(데이터길이bytes)
def get_bytes_stream(sock, length):
    buf = b''

    try:
        step = length-5

        while True:
            data = sock.recv(step)
            buf += data
            if len(buf) == length:
                break
            elif len(buf) < length:
                step = length - len(buf)
    except Exception as e:
        print(e)
    return buf[:length]

def write_utf8(s, sock):
    encoded = s.encode(encoding='utf-8')
    sock.sendall(len(encoded).to_bytes(4, byteorder="big"))
    sock.sendall(encoded)

def getLen(b,c,d,e):
    s1 = b & 0xff
    s2 = c & 0xff
    s3 = d & 0xff
    s4 = e & 0xff

    return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));


    #멀티쓰레드 (서버본문)

def threaded(client_sock, addr):
    print('Connected by', addr[0], ':', addr[1])

    while True:

        try:
            buf = b''
            length = 0

            data = client_sock.recv(5)
            buf += data

            ##buf[0]= 1->신규강의등록 2->카메라출석 3->
            if buf[0] == 1:    ##input_prof
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[1] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    print(len_bytes)
                    split_data = len_bytes.split(',', 4)
                    lecture_enroll(split_data)

            elif buf[0] == 2:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[2] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 데이터 수신') ##강의이름,교수이름,시작시간,종료시간,비밀번호

                # try:
                #     len_bytes_string = bytearray(client_sock.recv(length))
                #     len_bytes = len_bytes_string.decode("utf-8")
                #     if len_bytes == 'quit':
                #         print("데이터 받기 종료")
                #         break
                # except UnicodeDecodeError:
                #     print('quit 메세지가아님')
                # finally:
                # imgnum.txt에서 번호 가져와서 지정해주기
                # print("finally 실행")
                f1 = open("imgTemp/imgnum.txt", "rt")
                s1 = f1.read(5)  # s1는 파일이름번호 ex
                f1.close()

                f2 = open("imgTemp/imgnum.txt", "wt")
                f2.write(str(int(s1) + 1))
                f2.close()

                # 이미지파일 전송받기
                img_bytes = get_bytes_stream(client_sock, length)
                img_name = "img" + str(s1) + ".jpg"
                img_path = "imgTemp/img" + str(s1) + ".jpg"

                with open(img_path, "wb") as writer:
                    writer.write(img_bytes)

                test_path = "knn_examples/test"
                jpg_remove(test_path)  # test폴더 jpg파일 비우기

                #img_rotation('imgTemp', 270)

                jpg_copy("imgTemp","imgTemp/imgLogfile")
                jpg_move("imgTemp", test_path)
                jpg_remove("imgTemp")  # 서버에서 받는 이미지파일폴더 jpg파일 지우기

                for image_file in os.listdir(test_path):
                    print('dbg1')
                    full_file_path = os.path.join(test_path, image_file)

                    # 저장한 얼굴로 face.predict()실행 -> 누구얼굴인지 찾기
                    try:
                        predictions = face.predict(full_file_path, model_path="trained_knn_model.clf")
                    except PIL.UnidentifiedImageError:
                        print("얼굴 인식 실패")
                        client_sock.sendall("fail".encode('utf-8'))
                        print("데이터 반환 성공 fail 코드(얼굴 인식 실패)")

                    # Print results on the console
                    success = 0
                    for name, (top, right, bottom, left) in predictions:
                        success = 1
                        print("- Found {} at ({}, {})".format(name, left, top))

                        split = name.split(',', 1)
                        print("dbg1: name=" + split[0]+ " number=" + split[1] + " image= " + image_file)
                        resultcheck = 'failfind'

                        print("dbg 10 lecture_code = " + lecture_code)


                        resultcheck = checkAttendance(split[0], split[1], test_path+"/"+image_file, lecture_code) #인자= 이름,학번,사진데이터, 강의코드(5자리) 이름(성공), fail
                        

                        print("보낼데이터 resultcheck = " + resultcheck)
                        client_sock.sendall(resultcheck.encode('utf-8'))
                        print("데이터 반환 성공 안드로이드로")

                    if success == 0:
                        print("일치하는 얼굴 발견 실패")
                    # Display results overlaid on an image
                    #face.show_prediction_labels_on_image(os.path.join(test_path, image_file), predictions)

                # DB에서 해당정보 가져와서 다시 클라이언트로 전송
                ## write_utf8(img_path, client_sock)

            elif buf[0] == 3:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[3] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 사진')  ##학생이름,학번,학과,강의코드->student_data
                # imgnum.txt에서 번호 가져와서 지정해주기
                f1 = open("imgTemp/imgnum.txt", "rt")
                s1 = f1.read(5)  # s1는 파일이름번호 ex
                f1.close()

                f2 = open("imgTemp/imgnum.txt", "wt")
                f2.write(str(int(s1) + 1))

                # 이미지파일 전송받기
                img_bytes = get_bytes_stream(client_sock, length)
                img_name = "img" + str(s1) + ".jpg"
                img_path = "imgTemp/img" + str(s1) + ".jpg"

                with open(img_path, "wb") as writer:
                    writer.write(img_bytes)
                print(img_path + " is saved")

                ## 이미지를 모델에 입력해서 result에 detection 결과 저장하는 코드 ##
                # imgTemp에 저장

                # 저장한 사진에서 얼굴인식한 사진얻고 사진 버리기 or 로그폴더 저장
                face_finding(img_name, img_path)  # 자른 얼굴사진->face+imgnum+.jpg

                train_path = "knn_examples/train/"+student_data[0]+","+student_data[1]
                createFoler(train_path)

                file_list = os.listdir("./");

                for file in file_list:
                    if (file.endswith(".jpg")):
                        resultenroll = enroll_student(student_data, file)
                        if resultenroll == 0:
                            print('학생정보 저장완료')
                            client_sock.sendall("success".encode('utf-8'))
                        elif resultenroll == -1:
                            print('학생정보 저장실패')
                            client_sock.sendall("false".encode('utf-8'))

                jpg_move("./", train_path)  # test폴더로 잘라낸 얼굴이동

                jpg_move("imgTemp", "imgTemp/imgLogfile")  # 서버에서 받은 이미지파일 로그파일로이동
                jpg_remove("imgTemp")  # 서버에서 처음 받았던 이동하고남은 jpg파일 지우기

                # 2. KNN_classifier로 학습
                print("Training KNN classifier...")
                classifier = face.train("knn_examples/train", model_save_path="trained_knn_model.clf", n_neighbors=2)
                print("Training complete")

            elif buf[0] == 4:    ##login_prof
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[4] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    print(len_bytes)
                    split_data = len_bytes.split(',', 1)
                    login = lecture_login(split_data)
                    print("login ID is " + str(login) +"\nsending...\n")
                    if not bool(login):
                        client_sock.sendall("false".encode('utf-8'))
                    else:
                        client_sock.sendall(login.encode('utf-8'))

            elif buf[0] == 5:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[5] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    print(len_bytes)
                    Attend_prof = find_profAttendance(len_bytes)
                    lecture_info = find_lecturename(len_bytes) + Attend_prof

                    print(lecture_info + " has been sended")
                    client_sock.sendall(lecture_info.encode('utf-8'))

            elif buf[0] == 6:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[6] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    print(len_bytes)
                    split_data = len_bytes.split(',', 1)
                    print("data[0] = " + split_data[0] + " data[1] = " + split_data[1])
                    stuAttend = find_stuAttendance(split_data)
                    print(stuAttend + " has been sended")
                    client_sock.sendall(stuAttend.encode('utf-8'))


            elif buf[0] == 7:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[7] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    print(len_bytes)
                    student_data = len_bytes.split(',', 3)

            elif buf[0] == 8:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[8] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 문자')

                len_bytes_string = bytearray(client_sock.recv(length))
                ##바이트어레이로 전송할때 첫2바이트는 쓰레기값이 들어가서 제외##
                len_bytes = len_bytes_string.decode("utf-8")
                if len_bytes == 'quit':
                    print("데이터 받기 종료")
                    break
                else:
                    len_bytes = "Lq5IZ"  # 임시
                    print("lecture code is "+len_bytes)
                    lecture_code = len_bytes

            elif buf[0] == 9:
                print("---------------------------")
                print("접속시도: ", now.strftime('%Y-%m-%d %H:%M:%S'))
                print("buf[9] start")
                length = getLen(buf[1], buf[2], buf[3], buf[4])
                print(str(length) + 'bytes 데이터 수신')  ##강의이름,교수이름,시작시간,종료시간,비밀번호

                # try:
                #     len_bytes_string = bytearray(client_sock.recv(length))
                #     len_bytes = len_bytes_string.decode("utf-8")
                #     if len_bytes == 'quit':
                #         print("데이터 받기 종료")
                #         break
                # except UnicodeDecodeError:
                #     print('quit 메세지가아님')
                # finally:
                # imgnum.txt에서 번호 가져와서 지정해주기
                # print("finally 실행")
                f1 = open("imgTemp/imgnum.txt", "rt")
                s1 = f1.read(5)  # s1는 파일이름번호 ex
                f1.close()

                f2 = open("imgTemp/imgnum.txt", "wt")
                f2.write(str(int(s1) + 1))
                f2.close()

                # 이미지파일 전송받기
                img_bytes = get_bytes_stream(client_sock, length)
                img_name = "img" + str(s1) + ".jpg"
                img_path = "imgTemp/img" + str(s1) + ".jpg"

                with open(img_path, "wb") as writer:
                    writer.write(img_bytes)

                test_path = "knn_examples/test"
                jpg_remove(test_path)  # test폴더 jpg파일 비우기

                # img_rotation('imgTemp', 270)

                jpg_copy("imgTemp", "imgTemp/imgLogfile")
                jpg_move("imgTemp", test_path)
                jpg_remove("imgTemp")  # 서버에서 받는 이미지파일폴더 jpg파일 지우기

                for image_file in os.listdir(test_path):
                    print('dbg1')
                    full_file_path = os.path.join(test_path, image_file)

                    # 저장한 얼굴로 face.predict()실행 -> 누구얼굴인지 찾기
                    try:
                        print("dbg1123")
                        predictions = face.predict(full_file_path, model_path="trained_knn_model.clf")
                    except PIL.UnidentifiedImageError:
                        print("얼굴 인식 실패")
                        client_sock.sendall("fail".encode('utf-8'))
                        print("데이터 반환 성공 fail 코드(얼굴 인식 실패)")

                    print("dbg333")
                    # Print results on the console

                    success = 0
                    for name, (top, right, bottom, left) in predictions:
                        print("- Found {} at ({}, {})".format(name, left, top))

                        split = name.split(',', 1)
                        print("dbg1: name=" + split[0] + " number=" + split[1] + " image= " + image_file)

                        print("보낼데이터 resultcheck = " + name)
                        client_sock.sendall(name.encode('utf-8'))
                        print("데이터 반환 성공 안드로이드로")
                        success = 1

                    if success == 0:
                        client_sock.sendall("fail".encode('utf-8'))
                        print("얼굴 찾기 실패 fail코드 반환")
                    # Display results overlaid on an image
                    # face.show_prediction_labels_on_image(os.path.join(test_path, image_file), predictions)

                # DB에서 해당정보 가져와서 다시 클라이언트로 전송
                ## write_utf8(img_path, client_sock)



            # client_sock.send(name) #클라이언트에 인식된 이름전송
        except ConnectionResetError as e:
            print('Disconnected by' + str(addr))
            break

    print(addr[0] + '번지의 쓰레드종료\n______________________________\n\n')
    client_sock.close()



host = '192.168.0.18'##'220.124.24.89'##서버 IP##
port = 8080 ##서버 포트##

server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server_sock.bind((host, port))  #ip,포트  
server_sock.listen()

# 2. KNN_classifier로 학습
print("Training KNN classifier...")
classifier = face.train("knn_examples/train", model_save_path="trained_knn_model.clf", n_neighbors=2)
print("Training complete")

#서버부분
print("서버 시작!")
classify_team4 = 0;
lecture_code ="";
now = time

while True:
    print("기다리는 중")
    client_sock, addr = server_sock.accept()
    start_new_thread(threaded, (client_sock, addr))

server_sock.close()
