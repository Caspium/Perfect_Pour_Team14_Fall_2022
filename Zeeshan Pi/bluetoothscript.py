from bluetooth import *
import RPi.GPIO as GPIO

server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "b3f75a8f-fa4b-4dbc-8e79-51a486a30fa9"

messagequeue = []

advertise_service( server_sock, "BTS",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ] )


def begin_brewing():
    print("Brewing process instructions go here.")


def send_message(messagetosend):
    try:
        client_sock.send(messagetosend)
    except IOError:
        messagequeue.append(messagetosend)


while True:
    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)

    if len(messagequeue) > 0:
        print("There are messages to send. Sending them now.")
        for x in messagequeue:
            client_sock.send(x)

        messagequeue.clear()
    else:
        print("The message queue is empty.")

    try:
        while True:
            data = client_sock.recv(1024).split(":")
            print("received ", data[0])
            if data == "START_BREW":
                print("Starting Brew with ", data[1], " and ", data[2])
                send_message("STARTED_HEATING")
            elif data == "CONTINUE_BREWING":
                print("Continue Brewing Process...")

    except IOError:
        pass

    print("Disconnected")

    client_sock.close()
    server_sock.close()
    print("All Closed")
