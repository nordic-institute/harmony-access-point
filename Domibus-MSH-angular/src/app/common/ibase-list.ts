export interface IBaseList<T> {
  rows: Array<T>;
  count: number;
  readonly csvUrl: string;
}
