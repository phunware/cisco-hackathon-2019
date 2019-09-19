# Libraries
from pprint import pprint
from flask import Flask
from flask import json
from flask import request
import sys, getopt
import json
import requests
from lxml import etree
####################################################
app = Flask(__name__)


@app.route("/rooms", methods=["POST"], endpoint='rooms')
def post_message():
    global message_data
    API_ENDPOINT = "http://10.89.130.66/putxml"

    # if not request.json or not "data" in request.json:
    #     return ("invalid data", 400)

    message_data = request.json
    pprint(message_data, indent=1)

    # Validate message
    if 'message' in message_data:
        message_value = message_data["message"]
        configuration = etree.Element('Configuration')
        userinterface = etree.Element('UserInterface')
        customMessage = etree.Element('CustomMessage')
        customMessage.text = message_value
        userinterface.append(customMessage)
        configuration.append(userinterface)
        headers = {'Content-Type': 'application/xml','Authorization': 'Basic Y2lzddY286Y2lzY28='}
        data = etree.tostring(configuration, pretty_print=True)
        print(data)
        r = requests.post(url=API_ENDPOINT, data=data, headers=headers, verify=False)
        if(r.status_code==200):
            return (json.dumps({'status': 'Created'}), 200)
        return ("Error while talking to device ",400)
    elif 'halfwake_message' in message_data:
        message_value = message_data["halfwake_message"]
        configuration = etree.Element('Configuration')
        userinterface = etree.Element('UserInterface')
        osd = etree.Element('OSD')
        halfwake_message = etree.Element('HalfwakeMessage')
        halfwake_message.text = message_value
        osd.append(halfwake_message)
        userinterface.append(osd)
        configuration.append(userinterface)
        headers = {'Content-Type': 'application/xml','Authorization': 'Basic Y2lddzY286Y2lzY28='}
        data = etree.tostring(configuration, pretty_print=True)
        print(data)
        r = requests.post(url=API_ENDPOINT, data=data, headers=headers, verify=False)
        if(r.status_code==200):
            return (json.dumps({'status': 'Created'}), 200)
        return ("Error while talking to device ",400)
    elif 'url' in message_data:
        url_value = message_data["url"]
        command = etree.Element('Command')
        userinterface = etree.Element('UserInterface')
        webview = etree.Element('WebView')
        display = etree.Element('Display')
        url = etree.Element('Url')
        url.text = url_value
        display.append(url)
        webview.append(display)
        userinterface.append(webview)
        command.append(userinterface)
        headers = {'Content-Type': 'application/xml','Authorization': 'Basic Y2lzY286Y2lzY28='}
        data = etree.tostring(command, pretty_print=True)
        print(data)
        r = requests.post(url=API_ENDPOINT, data=data, headers=headers, verify=False)
        if(r.status_code==200):
            return (json.dumps({'status': 'Created'}), 200)
        return ("Error while talking to device ",400)
    else:
        return ("Request contain invalid information ", 400)

@app.route("/spaces", methods=["POST"], endpoint='spaces')
def post_message():
    global message_data
    global roomId
    API_ENDPOINT = "https://api.ciscospark.com/v1/messages"

    # if not request.json or not "data" in request.json:
    #     return ("invalid data", 400)

    message_data = request.json
    pprint(message_data, indent=1)

    if 'roomId' in message_data:
        roomId = message_data["roomId"]
    else:
        return ("Request contain invalid information ", 400)

    # Validate message
    if 'message' in message_data:
        message_value = message_data["message"]
        data=json.dumps({'roomId': roomId,'text':message_value})
        headers = {'Content-Type': 'application/json','Authorization': 'Bearer NTMcc1NWEwNTgtYjQwZS00ZDJmLWI0ZDgtZmVkODQ2OTBjZjdmODY1YTVkZGQtYjQz_PF84_4717db67-6e7a-46b9-b1dd-9d1b44830344'}
        r = requests.post(url=API_ENDPOINT, data=data, headers=headers, verify=False)
        if(r.status_code==200):
            return (json.dumps({'status': 'Created'}), 200)
        return ("Error while talking to device ",400)
    else:
        return ("Request contain invalid information ", 400)


def main(argv):

    try:
        opts, args = getopt.getopt(argv, "h")
    except getopt.GetoptError:
        print("cisco_device_api.py")
        sys.exit(2)
    for opt, arg in opts:
        if opt == "-h":
            print("cisco_device_api.py")
            sys.exit()


if __name__ == "__main__":
    main(sys.argv[1:])
    app.run(host="localhost", port=3000, debug=False)