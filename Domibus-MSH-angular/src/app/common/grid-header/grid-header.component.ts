import {Component,  Input, OnInit} from '@angular/core';
import {IPageableList} from '../mixins/ipageable-list';
import BaseListComponent from '../mixins/base-list.component';

@Component({
  selector: 'grid-header',
  templateUrl: './grid-header.component.html',
  styleUrls: ['./grid-header.component.css']
})
export class GridHeaderComponent implements OnInit {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & IPageableList;

  ngOnInit() {
  }

}
