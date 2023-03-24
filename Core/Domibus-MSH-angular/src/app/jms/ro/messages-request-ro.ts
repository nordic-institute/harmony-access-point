export class MessagesRequestRO {
  source: string;
  destination: string;
  originalQueue?: string;
  jmsType?: string;
  fromDate?: Date;
  toDate?: Date;
  selector?: string;
  action: string;
}
