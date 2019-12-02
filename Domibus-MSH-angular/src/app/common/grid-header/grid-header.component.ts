import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import {DomainService} from '../../security/domain.service';
import {Domain} from '../../security/domain';
import BaseListComponent from '../base-list.component';

@Component({
  selector: 'grid-header',
  templateUrl: './grid-header.component.html',
  styleUrls: ['./grid-header.component.css']
})
export class GridHeaderComponent implements OnInit {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any>;

  ngOnInit() {
  }

}
