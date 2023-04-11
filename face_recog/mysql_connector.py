import mysql.connector
import string
import random
import datetime
from datetime import datetime
from mysql.connector import Error

def lecture_login(data):
    try:
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234',
                                             charset='utf8'
                                             )
        cursor = connection.cursor()
        login_lecture = """SELECT lecture_pwd from lectureinfotbl WHERE lecture_name = '""" + data[0] + """'"""
        cursor.execute(login_lecture)
        result_id = cursor.fetchone()

        ##print(result_id)
        print("resultid is " + result_id[0])

        if result_id[0] == data[1]:
            login_lecture = """SELECT lecture_id from lectureinfotbl WHERE lecture_name = '""" + data[0] + """'"""
            cursor.execute(login_lecture)
            result_id = cursor.fetchone()
            return result_id[0]
        elif result_id[0] != data[1]:
            return False

        connection.commit()


    except mysql.connector.Error as error:
        print("Failed Searching login data into MySQL table {}".format(error))
    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")


def random_id():
    _LENGTH = 5 #강의id 길이
    string_pool = string.ascii_letters + string.digits

    # 랜덤한 문자열 생성
    result = ""
    for i in range(_LENGTH) :
        result += random.choice(string_pool) # 랜덤한 문자열 하나 선택
    return result

##innoDB내에 autoincrement오류 조심
##강의db: 강의id,수업이름,교수이름,수업시작시간,수업끝나는시간,수업비밀번호

def lecture_enroll(data):
    try:
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234',
                                             charset='utf8'
                                             )
        cursor = connection.cursor()

        while True:
            find_lectureid = """SELECT lecture_id from lectureinfotbl"""
            cursor.execute(find_lectureid)
            result_id = cursor.fetchall()
            for row in result_id:
                if row == random:
                    continue
            break

        insert_query = """ INSERT INTO lectureinfotbl
                                  (lecture_id,lecture_name,lecture_prof,lecture_start,lecture_end,lecture_pwd) VALUES (%s,%s,%s,%s,%s,%s)"""
        insert_tuple = (random, data[0], data[1], data[2], data[3], data[4])

        result_enroll = cursor.execute(insert_query, insert_tuple)
        print(insert_tuple)
        print("강의정보 등록 ", result_enroll)
        connection.commit()

    except mysql.connector.Error as error:
        print("Failed inserting lecture information data into MySQL table {}".format(error))
    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")


def convertToBinaryData(filename):
    # Convert digital data to binary format
    with open(filename, 'rb') as file:
        binaryData = file.read()
    return binaryData

# 수정중 학생이름,학번,학과,강의코드->data
def enroll_student(data, photo):
    print("Inserting BLOB into images table")
    try:
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234')

        cursor = connection.cursor()
        sql_insert_blob_query = """ INSERT INTO studenttbl
                          (stu_name, stu_id, major, lecture_id, face_pic) VALUES (%s,%s,%s,%s,%s)"""

        Picture = convertToBinaryData(photo)

        # Convert data into tuple format
        insert_blob_tuple = (data[0], data[1], data[2], data[3], Picture)
        result = cursor.execute(sql_insert_blob_query, insert_blob_tuple)
        connection.commit()
        print("Image and file inserted successfully into student table", result)
        return 0

    except mysql.connector.Error as error:
        print("Failed inserting into student table {}".format(error))
        return -1

    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")

def find_stuAttendance(split):
    try:
        print("---Mysql(CheckAttendance)--------------------")
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234')

        cursor = connection.cursor()
        print("Mysql Connection Success")
        print("dbg1: split[0] is " + split[0] + ", split[1] is " + split[1])

        #IFNULL(attendance_id, "0") as
        data ="";
        find_Attend = """SELECT attendance_date, attendance_code from attendancetbl WHERE attendance_name = '""" + split[0] + """' AND attendance_number = '""" + split[1] + """'  """
        cursor.execute(find_Attend)
        result = cursor.fetchall()

        exceed = 0;
        for row in result:
            data = data + split[0] + "," + split[1] + "," + row[0].strftime("%Y-%m-%d") +"," + row[1] +"/"
            exceed += 1
        print("data is ==> " + data)

        if exceed == 0:
            return "fail"
        else:
            return data


    except mysql.connector.Error as error:
        print("Failed inserting BLOB data {}".format(error))

    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            print("------------------------------------------")

def find_lecturename(code):
    try:
        print("---Mysql(CheckAttendance)--------------------")
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234')

        cursor = connection.cursor('Buffered=True')
        print("Mysql Connection Success")

        data = "";
        find_code = """SELECT lecture_name, lecture_prof from lectureinfotbl WHERE lecture_id = '""" + code + """' """
        cursor.execute(find_code)
        result_code = cursor.fetchall()

        exceed = 0;
        for row in result_code:
            data = data + row[0] + "#" + row[1] + '#'
            exceed += 1

        if exceed == 0:
            return "fail"
        else:
            return data

    except mysql.connector.Error as error:
        print("Failed inserting BLOB data {}".format(error))

    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            print("------------------------------------------")

def find_profAttendance(code):
    try:
        print("---Mysql(CheckAttendance)--------------------")
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234')

        cursor = connection.cursor('Buffered=True')
        print("Mysql Connection Success")

        data = "";
        find_code = """SELECT attendance_name, attendance_number, attendance_date from attendancetbl WHERE attendance_code = '""" + code + """' ORDER BY attendance_date DESC"""
        cursor.execute(find_code)
        result_code = cursor.fetchall()

        exceed = 0;
        for row in result_code:
            data = data + row[0] + "," + str(row[1]) + "," + row[2].strftime("%Y-%m-%d")+"/"
            exceed += 1

        if exceed == 0:
            return "fail"
        else:
            return data

    except mysql.connector.Error as error:
        print("Failed inserting BLOB data {}".format(error))

    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            print("------------------------------------------")

def checkAttendance(name, number, photo, code):
    try:
        print("---Mysql(CheckAttendance)--------------------")
        connection = mysql.connector.connect(host='127.0.0.1',
                                             database='attendance',
                                             user='root',
                                             password='1234')

        cursor = connection.cursor('Buffered=True')
        print("Mysql Connection Success")

        today = datetime.now().date().strftime("%Y-%m-%d")

        # find_code = """SELECT lecture_id from studenttbl WHERE stu_name = '""" + name + """' AND stu_id = '""" + number + """'  """
        # cursor.execute(find_code)
        # result_code = cursor.fetchall()
        # for row in result_code:
        #     print("row is " + row[0])
        #     lecture_code = row[0]            #bug 버그여기하나
        #
        #     if row[0] is None:
        #         print("sql fail")
        #         return "failfind"
        #     break


        print("db1: 오늘은 " + today)
        print("db2: 코드 받아온거" + code)
        #IFNULL(attendance_id, "0") as
        find_Attend = """SELECT attendance_id, attendance_date from attendancetbl WHERE attendance_date = '""" + \
                      today + """' AND attendance_name = '""" + name + """' AND attendance_number = '""" + number + """' AND attendance_code = '""" + code + """'  """
        cursor.execute(find_Attend)
        result = cursor.fetchall() # 이름,학번,수업코드가 같은친구로 출석id랑 출석날 데이터 튜플을 다 가져와

        exceed = 0
        for row in result: #오늘 출석한 데이터가 없으면 for문 아예실행안됨 = fail 리턴이 안됨
            print("응 실행됨")
            exceed = 1 #만약 출석을 했으면 cuz for문으로 돌렸을때 값이 nullable이면 안에가 아예 실행안됨 exceed가1이면 출석데이터가있음
            return "fail" #출석date가 오늘date랑 같다면 (둘이 같은format) fail값 리턴하고 함수자체가 끝

        if exceed == 0:
            sql_insert_blob_query = """ INSERT INTO attendancetbl
                                                          (attendance_date, attendance_name, attendance_number,attendance_face,attendance_code) VALUES (%s,%s,%s,%s,%s)"""

            print("dbg4: 여기는실행됨")
            Picture = convertToBinaryData(photo)
            # Convert data into tuple format
            insert_blob_tuple = (today, name, number, Picture, code)
            result_insert = cursor.execute(sql_insert_blob_query, insert_blob_tuple)
            connection.commit()
            print("inserted successfully as", name)
            return name


    except mysql.connector.Error as error:
        print("Failed inserting BLOB data {}".format(error))

    finally:
        if (connection.is_connected()):
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            print("------------------------------------------")
