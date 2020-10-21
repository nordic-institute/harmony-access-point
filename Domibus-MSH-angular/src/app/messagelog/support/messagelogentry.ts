export class MessageLogEntry {
  constructor(public messageId: string,
              public mshRole: string,
              public conversationId: string,
              public messageType: string,
              public messageStatus: string,
              public notificationStatus: string,
              public fromPartyId: string,
              public toPartyId: string,
              public originalSender: string,
              public finalRecipient: string,
              public refToMessageId: string,
              public receivedFrom: Date,
              public receivedTo: Date,
              public isTestMessage: boolean
  ) {

  }
}
