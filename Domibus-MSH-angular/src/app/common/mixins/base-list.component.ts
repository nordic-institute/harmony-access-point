import {AlertComponent} from '../alert/alert.component';
import {AlertService} from '../alert/alert.service';
import {DownloadService} from '../download.service';
import {OnInit} from '@angular/core';
import {ColumnPickerBase} from '../column-picker/column-picker-base';
import {IBaseList} from './ibase-list';
import {instanceOfFilterableList, instanceOfModifiableList} from './type.utils';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * Base class for list components;
 */
export interface Constructable {
  new(...args);
}

export function ConstructableDecorator(constructor: Constructable) {
}

@ConstructableDecorator
export default class BaseListComponent<T> implements IBaseList<T>, OnInit {
  public rows: T[];
  public count: number;
  public columnPicker: ColumnPickerBase;

  constructor(private alertService: AlertService) {
    this.columnPicker = new ColumnPickerBase();
  }

  ngOnInit(): void {
    this.rows = [];
    this.count = 0;
  }

  public get name(): string {
    return undefined;
  }

  public get csvUrl(): string {
    return undefined;
  }

  public async saveAsCSV() {
    if (instanceOfModifiableList(this)) {
      await this.saveIfNeeded();
    }

    if (this.count > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }

    DownloadService.downloadNative(this.csvUrl);
  }

  protected hasMethod(name: string) {
    return this[name] && this[name] instanceof Function;
  }

};



