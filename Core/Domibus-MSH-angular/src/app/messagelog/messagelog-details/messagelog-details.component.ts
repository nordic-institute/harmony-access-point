import {Component, OnInit, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {HttpClient, HttpParams} from '@angular/common/http';
import {CustomURLEncoder} from '../../common/custom-url-encoder';
import {MessageLogComponent} from '../messagelog.component';
import {MessageLogResult} from '../support/messagelogresult';

@Component({
  selector: 'app-messagelog-details',
  templateUrl: './messagelog-details.component.html'
})
export class MessagelogDetailsComponent implements OnInit {

  message;

  constructor(public dialogRef: MatDialogRef<MessagelogDetailsComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private http: HttpClient) {
    this.message = data.message;
  }

  async ngOnInit() {
    if (!this.data.fetchData || !this.message) {
      return;
    }
    let filterParams = new HttpParams({encoder: new CustomURLEncoder()});
    filterParams = filterParams.append('messageType', this.message['messageType'])
    filterParams = filterParams.append('messageId', this.message['messageId'])
    filterParams = filterParams.append('mshRole', this.message['mshRole'])
    filterParams = filterParams.append('applyDefaultFilters', 'false')
    this.data.fields.forEach(field => filterParams = filterParams.append('fields', field));
    const res = <MessageLogResult>await this.http.get<any>(MessageLogComponent.MESSAGE_LOG_URL, {params: filterParams}).toPromise();
    if (res && res.count == 1 && res.messageLogEntries && res.messageLogEntries.length > 0) {
      this.message = res.messageLogEntries[0];
    }
  }
}
