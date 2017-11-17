 /**
 *  Laundry Notifier
 *
 *  Copyright 2017 Justin Walker
 *
 *  Requires Aeon Smart Energy Switch 6 custom DH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
definition(
    name: "Laundry Notifier",
    namespace: "augoisms",
    author: "augoisms",
    description: "Monitor the laundry status and send a notification when the cycle is complete",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances1-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances1-icn@2x.png"
)

preferences {
	section("Select machines") {
		input "washer", "capability.powerMeter", title: "Washer", required: true
        input "dryer", "capability.powerMeter", title: "Dryer", required: true
	}
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
            input "repeat", "bool", title: "Repeat notifications?"
            input "repeatInterval", "number", title: "Repeat interval (minutes)", defaultValue: 15
        }
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(washer, "status", washerHandler)
    subscribe(dryer, "status", dryerHandler) 
}

def washerHandler(evt) {
	log.debug "checking washer status"    
    statusCheck(washer, "Washer", "washerHandler")
}

def dryerHandler(evt) {
	log.debug "checking dryer status"    
    statusCheck(dryer, "Dryer", "dryerHandler")
}

def statusCheck(device, deviceName, handlerName) {
	def status = device.currentValue("status")
  
    if (status == "finished") {
    	// send notification
        send("${deviceName} is finished")
        // schedule repeat notification
        if(repeat) {
        	log.debug "scheduling a repeat notification"
            runIn(repeatInterval * 60, handlerName)
        }
    }
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }
    
    log.debug msg
}