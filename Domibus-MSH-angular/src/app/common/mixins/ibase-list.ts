import {ColumnPickerBase} from '../column-picker/column-picker-base';

export interface IBaseList<T> {
  rows: Array<T>;
  count: number;
  readonly csvUrl: string;
  columnPicker: ColumnPickerBase; //here for now
}
