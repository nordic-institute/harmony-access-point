import {Component, Input, OnInit} from '@angular/core';
import {DomibusInfoService} from '../appinfo/domibusinfo.service';
import {DomibusInfo} from '../appinfo/domibusinfo';
import BaseListComponent from '../mixins/base-list.component';
import {IFilterableList} from '../mixins/ifilterable-list';

@Component({
  templateUrl: 'page-footer.component.html',
  selector: 'page-footer',
  styleUrls: ['./page-footer.component.css']
})

export class PageFooterComponent implements OnInit {

  constructor(private domibusInfoService: DomibusInfoService) {
  }

  @Input()
  parent: BaseListComponent<any>;

  async ngOnInit() {
  }

}
