import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material';
import {ResolveEnd, Router} from '@angular/router';
import {isNullOrUndefined} from 'util';
import {DomibusInfoService} from '../appinfo/domibusinfo.service';


@Component({
  selector: 'page-helper',
  templateUrl: './page-helper.component.html',
  styleUrls: ['./page-helper.component.css']
})
export class PageHelperComponent implements OnInit {

  pageName: string;
  helpPages: Map<String, String> = new Map<String, String>();
  activateHelp = false;

  constructor(public dialog: MatDialog, private router: Router, private domibusInfoService: DomibusInfoService) {
  }

  async ngOnInit() {
    await this.setHelpPages();

    this.router.events.subscribe(event => this.processRouteChange(event));
  }

  private processRouteChange(event) {
    if (event instanceof ResolveEnd) {
      const url = event.url.split('?')[0];
      const page = this.helpPages.get(url);

      if (isNullOrUndefined(page)) {
        this.activateHelp = false;
      } else {
        this.activateHelp = true;
        this.pageName = page.toString();
      }
    }
  }

  private async setHelpPages() {
    const domibusInfo = await this.domibusInfoService.getDomibusInfo();
    const MAIN_HELP_PAGE = `https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus+v${domibusInfo.versionNumber}+Admin+Console+Help`;
    const VERSION_SPECIFIC_PAGE = `#Domibusv${domibusInfo.versionNumber}AdminConsoleHelp-`;

    const routes = this.router.config;
    routes.forEach(route => {
      if (route.data && route.data.helpPage) {
        this.helpPages.set('/' + route.path, MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + route.data.helpPage);
      }
    })
  }

  openHelpDialog() {
    window.open(this.pageName, '_blank');
  }

}
