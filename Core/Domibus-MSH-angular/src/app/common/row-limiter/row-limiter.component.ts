import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-row-limiter',
  templateUrl: './row-limiter.component.html',
  styleUrls: ['./row-limiter.component.css']
})
export class RowLimiterComponent implements OnInit {
  @Input()
  pageSizes: Array<any>;

  @Output()
  private onPageSizeChanged = new EventEmitter<number>();

  // this is a property of type function that returns a promise;
  // created like this to communicate between parent and child components both ways
  @Input()
  private onPageSizeChanging: any;

  _pageSize: number;
  _oldPageSize: number;

  constructor() {
  }

  ngOnInit() {
    this._pageSize = this.pageSizes[0].value;
    this._oldPageSize = -1;
  }

  set pageSize(value) {
    this._oldPageSize = this._pageSize;
    this._pageSize = value;
  }

  get pageSize() {
    return this._pageSize;
  }

  changePageSize(newPageLimit: any) {
    if (this.onPageSizeChanging) {
      this.onPageSizeChanging(newPageLimit.value).then((cancel) => {
        if (cancel) {
          this.pageSize = this._oldPageSize;
        } else {
          this.onPageSizeChanged.emit(newPageLimit);
        }
      });
    } else {
      this.onPageSizeChanged.emit(newPageLimit);
    }
  }

}
