import {Component, Input} from '@angular/core';
import {IPageableList, PaginationType} from '../mixins/ipageable-list';
import BaseListComponent from '../mixins/base-list.component';
import {ISortableList} from '../mixins/isortable-list';
import {instanceOfPageableList, instanceOfSortableList} from '../mixins/type.utils';

@Component({
  selector: 'page-grid',
  templateUrl: './page-grid.component.html',
  styleUrls: ['./page-grid.component.css']
})

export class PageGridComponent {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & IPageableList & ISortableList;

  @Input()
  selectionType: undefined | 'single' | 'multi' = undefined;

  @Input()
  sortedColumns: { prop: string, dir: string }[] = [];

  @Input()
  rowClassFn: Function;

  useExternalPaging() {
    return instanceOfPageableList(this.parent) && this.parent.type != PaginationType.Client;
  }

  useExternalSorting() {
    return instanceOfSortableList(this.parent);
  }

}
