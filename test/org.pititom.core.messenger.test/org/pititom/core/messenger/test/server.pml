identifier: org.pititom.core.test.messenger.server
auto connect: false
receiver: org.pititom.core.messenger.stream.StreamReceiver
configuration :
  identifier: org.pititom.core.test.messenger.server.receiver
  input stream: org.pititom.core.stream.UdpIpInputStream
  configuration :
    destination inet socket address: 230.2.15.2:5200
    max packet size: 64
    bind: true
    reuse: true
  over: org.pititom.core.messenger.test.TestObjectInputStream
sender: org.pititom.core.messenger.stream.StreamSender
configuration :
  identifier: org.pititom.core.test.messenger.server.sender
  output stream: org.pititom.core.stream.UdpIpOutputStream
  configuration: 
    destination inet socket address: 230.2.15.2:5200
    max packet size: 64
    bind: true
    reuse: true
  over: org.pititom.core.messenger.test.TestObjectOutputStream
