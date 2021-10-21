import {ColumnPickerBase} from '../column-picker/column-picker-base';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the base List
 * */
export interface IBaseList<T> {
  name: string;
  rows: T[];
  selected: T[];
  count: number;
  readonly csvUrl: string;
  columnPicker: ColumnPickerBase;
  isLoading: boolean;

  isBusy(): boolean;

  getDataAndSetResults(): Promise<any>;

  loadServerData(): Promise<any>;

  onActivate($event);

  showDetails(row: T)
}
