import {Component, Inject, OnInit, Input} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {MessageLogEntry} from 'app/messagelog/support/messagelogentry';
import {AlertService} from 'app/common/alert/alert.service';
import {ConnectionsMonitorService} from '../support/connectionsmonitor.service';

/**
 * @author Tiago MIGUEL
 * @since 4.0
 *
 * Test service form for a single party.
 */

@Component({
  moduleId: module.id,
  templateUrl: 'connection-details.component.html',
  styleUrls: ['connection-details.component.css'],
  providers: [ConnectionsMonitorService]
})

export class ConnectionDetailsComponent implements OnInit {

  static readonly MESSAGE_LOG_LAST_TEST_SENT_URL: string = 'rest/messagelog/test/outgoing/latest';
  static readonly MESSAGE_LOG_LAST_TEST_RECEIVED_URL: string = 'rest/messagelog/test/incoming/latest';

  @Input() partyId: string;
  sender: string;

  messageInfoSent: MessageLogEntry;
  messageInfoReceived: MessageLogEntry;


  constructor(private connectionsMonitorService: ConnectionsMonitorService, private http: HttpClient, private alertService: AlertService,
              public dialogRef: MatDialogRef<ConnectionDetailsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.partyId = data.partyId;
  }

  async ngOnInit() {
    this.sender = '';

    this.clearInfo();
    await this.getSenderParty();
    if (this.sender && this.partyId) {
      this.update();
    }
  }

  async test() {
    this.clearInfo();
    try {
      this.messageInfoSent.messageId = await this.connectionsMonitorService.sendTestMessage(this.partyId, this.sender);
      this.alertService.success('Test Message Sent Successfully. Please press Update button to refresh and receive the response!');
      this.update();
    } catch (err) {
      this.alertService.exception('Problems while submitting test', err)
    }
  }

  async update() {
    this.alertService.clearAlert();
    await this.getLastSentRequest(this.partyId);
    if (this.messageInfoSent.messageId) {
      await this.getLastReceivedRequest(this.partyId, this.messageInfoSent.messageId);
    }
  }

  private clearInfo() {
    this.messageInfoSent = new MessageLogEntry('', '', '', '', '', '', '', '', '', '', '', null, null, false);
    this.messageInfoReceived = new MessageLogEntry('', '', '', '', '', '', '', '', '', '', '', null, null, false);
  }

  async getSenderParty() {
    try {
      this.sender = await this.connectionsMonitorService.getSenderParty();
    } catch (error) {
      this.sender = '';
      this.alertService.exception('The test service is not properly configured.', error);
    }
  }

  async getLastSentRequest(partyId: string) {
    try {
      let searchParams = new HttpParams();
      searchParams = searchParams.append('partyId', partyId);

      let result = await this.http.get<any>(ConnectionDetailsComponent.MESSAGE_LOG_LAST_TEST_SENT_URL, {params: searchParams}).toPromise();
      if (result) {
        this.alertService.clearAlert();
        this.messageInfoSent.toPartyId = result.partyId;
        this.messageInfoSent.finalRecipient = result.accessPoint;
        this.messageInfoSent.receivedTo = new Date(result.timeReceived);
        this.messageInfoSent.messageId = result.messageId;
      }
    } catch (err) {
      this.alertService.exception(`Error retrieving Last Sent Test Message for ${partyId}`, err);
    }
  }

  async getLastReceivedRequest(partyId: string, userMessageId: string) {
    try {
      let searchParams = new HttpParams();
      searchParams = searchParams.append('partyId', partyId);
      searchParams = searchParams.append('userMessageId', userMessageId);
      let result = await this.http.get<any>(ConnectionDetailsComponent.MESSAGE_LOG_LAST_TEST_RECEIVED_URL, {params: searchParams}).toPromise();

      if (result) {
        this.messageInfoReceived.fromPartyId = partyId;
        this.messageInfoReceived.originalSender = result.accessPoint;
        this.messageInfoReceived.receivedFrom = new Date(result.timeReceived);
        this.messageInfoReceived.messageId = result.messageId;
      }
    } catch (err) {
      this.alertService.exception(`Error retrieving Last Received Test Message for ${partyId}`, err);
    }
  }

}
