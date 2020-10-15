import {Component, Input, OnInit} from '@angular/core';
import {SecurityService} from '../../security/security.service';
import {DomainService} from '../../security/domain.service';
import {Domain} from '../../security/domain';
import {MatDialog} from '@angular/material';
import {AlertService} from '../alert/alert.service';
import {ActivatedRoute, ActivatedRouteSnapshot, Router, RoutesRecognized} from '@angular/router';
import {DialogsService} from '../dialogs/dialogs.service';
import {instanceOfModifiableList} from '../mixins/type.utils';

@Component({
  selector: 'domain-selector',
  templateUrl: './domain-selector.component.html',
  styleUrls: ['./domain-selector.component.css']
})
export class DomainSelectorComponent implements OnInit {

  showDomains: boolean;
  displayDomains: boolean;
  currentDomainCode: string;
  domainCode: string;
  domains: Domain[];

  @Input()
  currentComponent: any;

  constructor(private domainService: DomainService,
              private securityService: SecurityService,
              private dialog: MatDialog,
              private dialogsService: DialogsService,
              private alertService: AlertService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  async ngOnInit() {
    try {
      const isMultiDomain = await this.domainService.isMultiDomain().toPromise();

      if (isMultiDomain && this.securityService.isCurrentUserSuperAdmin()) {
        this.domains = await this.domainService.getDomains();
        this.displayDomains = true;
        this.showDomains = this.shouldShowDomains(this.route.snapshot);

        this.domainService.getCurrentDomain().subscribe(domain => {
          this.domainCode = this.currentDomainCode = (domain ? domain.code : null);
        });
      }

      this.router.events.subscribe(event => {
        if (event instanceof RoutesRecognized) {
          let route = event.state.root.firstChild;
          this.showDomains = this.shouldShowDomains(route);
        }
      });
    } catch (error) {
      console.log('error while calling backend for getting domains information: ' + error);
    }
  }

  private shouldShowDomains(route: ActivatedRouteSnapshot) {
    return this.displayDomains && !route.data.isDomainIndependent;
  }

  async changeDomain() {
    this.alertService.clearAlert();

    try {
      const canChange = await this.canChangeDomain();
      if (!canChange) {
        throw false;
      }

      if (this.currentComponent.beforeDomainChange) {
        try {
          this.currentComponent.beforeDomainChange();
        } catch (e) {
          console.log('Exception raised in before domain change code', e);
        }
      }

      const domain = this.domains.find(d => d.code === this.domainCode);
      await this.domainService.setCurrentDomain(domain);

      this.alertService.clearAlert();

      this.domainService.setAppTitle();

      if (this.currentComponent.ngOnInit) {
        try {
          this.currentComponent.ngOnInit();
        } catch (e) {
          this.alertService.exception('Error in init code', e);
        }
      }

      if (this.currentComponent.ngAfterViewInit) {
        try {
          this.currentComponent.ngAfterViewInit();
        } catch (e) {
          this.alertService.exception('Error in after view init code', e);
        }
      }

    } catch (ex) { // domain not changed -> reset the combo value
      this.domainCode = this.currentDomainCode;
      if (ex.status <= 0) {
        this.alertService.exception('The server didn\'t respond, please try again later', ex);
      } else {
        this.alertService.exception('Error trying to chenge the domain: ', ex);
      }
    }
  }

  private async canChangeDomain() {

    return this.securityService.canAbandonUnsavedChanges(this.currentComponent);
  }

}

