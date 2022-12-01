from bluetooth import *
import RPi.GPIO as GPIO
import time

uuid = "b3f75a8f-fa4b-4dbc-8e79-51a486a30fa9"

# Place a message here to have it sent to app at start
messagequeue = []

#Basic Framework of brew process
def begin_brewing():
    print("Brewing process instructions go here.")
    send_message("STARTED_HEATING")
    time.sleep(5)
    send_message("STARTED_POURING")
    time.sleep(5)
    send_message("BREWING_COMPLETE")

#send message to app
# STARTED_HEATING and STARTED_POURING denote stage
# BREWING_COMPLETE will end connection with app
# any other messages sent will display message to user
# other messages will also end connection with app as they are assumed to be irrecoverable errors
def send_message(messagetosend):
    try:
        client_sock.send(messagetosend)
    except IOError:
        messagequeue.append(messagetosend)


while True:
    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("",PORT_ANY))
    server_sock.listen(1)
    
    port = server_sock.getsockname()[1]
    
    advertise_service( server_sock, "BTS",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ] )

    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)
    # send any outstanding messages
    if len(messagequeue) > 0:
        print("There are messages to send. Sending them now.")
        for x in messagequeue:
            client_sock.send(x)

        messagequeue.clear()
    else:
        print("The message queue is empty.")

    try:
        while True:
            data = client_sock.recv(1024).decode("utf-8")
            if len(data) == 0: break
            print("Received:", data)
            ndata = data.split(':')
            if ndata[0] == "START_BREW":
                print("Starting Brew with", ndata[1], "and", ndata[2])
                begin_brewing()
            elif ndata[0] == "CONTINUE_BREWING":
                print("Continue Brewing Process...")

    except IOError:
        pass

    print("Disconnected")

    client_sock.close()
    server_sock.close()
    print("All Closed")
