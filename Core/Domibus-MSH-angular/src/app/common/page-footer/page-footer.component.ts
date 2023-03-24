import {Component, Input, OnInit} from '@angular/core';
import BaseListComponent from '../mixins/base-list.component';
import {IModifiableList} from '../mixins/imodifiable-list';

@Component({
  templateUrl: 'page-footer.component.html',
  selector: 'page-footer',
  styleUrls: ['./page-footer.component.css']
})

export class PageFooterComponent implements OnInit {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & IModifiableList;

  @Input()
  isAddVisible: boolean = true;

  @Input()
  isEditVisible: boolean = true;

  async ngOnInit() {
  }

}
