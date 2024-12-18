import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDialogModule} from '@angular/material/dialog';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTooltipModule} from '@angular/material/tooltip';

import {NgxDatatableModule} from '@swimlane/ngx-datatable';
import {AppComponent} from './app.component';
import {LoginComponent} from './security/login/login.component';
import {CurrentPModeComponent} from './pmode/current/currentPMode.component';
import {PModeArchiveComponent} from './pmode/archive/pmodeArchive.component';

import {AuthenticatedAuthorizedGuard} from './common/guards/authenticated-authorized.guard';
import {routing} from './app.routes';
import {ExtendedHttpInterceptor} from './common/http/extended-http-client';
import {HttpEventService} from './common/http/http.event.service';
import {SecurityService} from './security/security.service';
import {SecurityEventService} from './security/security.event.service';
import {DomainService} from './security/domain.service';
import {AlertComponent} from './common/alert/alert.component';
import {AlertService} from './common/alert/alert.service';
import {ErrorLogComponent} from './errorlog/errorlog.component';
import {DomibusInfoService} from './common/appinfo/domibusinfo.service';
import {MessageFilterComponent} from './messagefilter/messagefilter.component';
import {MessageLogComponent} from './messagelog/messagelog.component';
import {UserComponent} from './user/user.component';
import {BaseTruststoreComponent} from './truststore/base-truststore.component';
import {PmodeUploadComponent} from './pmode/upload/pmode-upload.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {JmsComponent} from './jms/jms.component';
import {RowLimiterComponent} from './common/row-limiter/row-limiter.component';
import {MoveDialogComponent} from './jms/move-dialog/move-dialog.component';
import {MessageDialogComponent} from './jms/message-dialog/message-dialog.component';
import {DomibusDatePipe} from './common/customDate/domibusDatePipe';
import {CapitalizeFirstPipe} from './common/capitalizefirst.pipe';
import {DefaultPasswordDialogComponent} from './security/default-password-dialog/default-password-dialog.component';
import {MessagelogDetailsComponent} from './messagelog/messagelog-details/messagelog-details.component';
import {ErrorlogDetailsComponent} from './errorlog/errorlog-details/errorlog-details.component';
import {EditMessageFilterComponent} from './messagefilter/editmessagefilter-form/editmessagefilter-form.component';
import {YesNoDialogComponent} from './common/dialogs/yes-no-dialog/yes-no-dialog.component';
import {DirtyGuard} from './common/guards/dirty.guard';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {TruststoreDialogComponent} from './truststore/truststore-dialog/truststore-dialog.component';
import {TrustStoreUploadComponent} from './truststore/truststore-upload/truststore-upload.component';
import {ColumnPickerComponent} from './common/column-picker/column-picker.component';
import {PageHelperComponent} from './common/page-helper/page-helper.component';
import {SharedModule} from './common/module/SharedModule';
import {ActionDirtyDialogComponent} from './pmode/action-dirty-dialog/action-dirty-dialog.component';
import {AuditComponent} from './audit/audit.component';
import {PartyComponent} from './party/party.component';
import {PartyDetailsComponent} from './party/party-details/party-details.component';
import {PageHeaderComponent} from './common/page-header/page-header.component';
import {DomainSelectorComponent} from './common/domain-selector/domain-selector.component';
import {PmodeViewComponent} from './pmode/archive/pmode-view/pmode-view.component';
import {AlertsComponent} from './alerts/alerts.component';
import {PluginUserComponent} from './pluginuser/pluginuser.component';
import {EditBasicPluginUserFormComponent} from './pluginuser/editpluginuser-form/edit-basic-plugin-user-form.component';
import {
  EditCertificatePluginUserFormComponent
} from './pluginuser/editpluginuser-form/edit-certificate-plugin-user-form.component';
import {PartyIdentifierDetailsComponent} from './party/party-identifier-details/party-identifier-details.component';
import {GlobalErrorHandler} from './common/global.error-handler';
import {UserService} from './user/support/user.service';
import {UserValidatorService} from './user/support/uservalidator.service';
import {DefaultPasswordGuard} from './common/guards/defaultPassword.guard';
import {SanitizeHtmlPipe} from './common/sanitizeHtml.pipe';
import {LoggingComponent} from './logging/logging.component';
import {ChangePasswordComponent} from './security/change-password/change-password.component';
import {AuthExternalProviderGuard} from './common/guards/auth-external-provider.guard';
import {LogoutAuthExtProviderComponent} from './security/logout/logout.components';
import {RedirectHomeGuard} from './common/guards/redirect-home.guard';
import {NotAuthorizedComponent} from './security/not-authorized/not-authorized.components';
import {PropertiesComponent} from './properties/properties.component';
import {CommonModule, DatePipe, DecimalPipe} from '@angular/common';
import {DialogsService} from './common/dialogs/dialogs.service';
import {GridHeaderComponent} from './common/grid-header/grid-header.component';
import {FilterAreaFooterComponent} from './common/filter-area-footer/filter-area-footer.component';
import {PageFooterComponent} from './common/page-footer/page-footer.component';
import {PageGridComponent} from './common/page-grid/page-grid.component';
import {UserCrossFieldValidatorsDirective} from './common/directive/user-cross-field-validators.directive';
import {EditPluginUserFormBaseComponent} from './pluginuser/editpluginuser-form/edit-plugin-user-form-base.component';
import {EditPopupFooterComponent} from './common/popup-edit-footer/edit-popup-footer.component';
import {EditPopupBaseComponent} from './common/edit-popup-base.component';
import {PropertiesService} from './properties/support/properties.service';
import {FileUploadValidatorService} from './common/file-upload-validator.service';
import {ConnectionsComponent} from './testservice/connections.component';
import {ConnectionDetailsComponent} from './testservice/connection-details/connection-details.component';
import {ApplicationContextService} from './common/application-context.service';
import {SessionExpiredDialogComponent} from './security/session-expired-dialog/session-expired-dialog.component';
import {SessionService} from './security/session.service';
import {AuthInternalProviderGuard} from './common/guards/auth-internal-provider.guard';
import {
  AddNestedPropertyDialogComponent
} from './properties/support/add-nested-property-dialog/add-nested-property-dialog.component';
import {TLSTruststoreComponent} from './truststore/tls.truststore.component';
import {TruststoreComponent} from './truststore/truststore.component';
import {KeystoreComponent} from './truststore/keystore.component';
import {CertificateUploadComponent} from './truststore/certificate-upload/certificate-upload.component';
import {PluginUserValidatorService} from './pluginuser/support/pluginuservalidator.service';
import {DomibusFutureDatePipe} from './common/customDate/domibusFutureDatePipe';
import {DomainsComponent} from './domains/domains.component';
import {DataTablePagerComponent} from './common/page-grid/datatable-pager.comonent';
import {LandingPageGuard} from './common/guards/landing-page.guard';
import {OkDialogComponent} from './common/dialogs/ok-dialog/ok-dialog.component';
import {
  NGX_MAT_DATE_FORMATS,
  NgxMatDateFormats,
  NgxMatDatetimePickerModule
} from '@angular-material-components/datetime-picker';
import {NgxMatMomentModule} from '@angular-material-components/moment-adapter';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {ManageBackendsComponent} from './messagefilter/manageBackends-form/manageBackends-form.component';
import {DateService} from './common/customDate/date.service';
import {HelperService} from './common/helper.service';

const CUSTOM_MOMENT_FORMATS: NgxMatDateFormats = {
  parse: {
    dateInput: 'DD/MM/YYYY HH:mm'
  },
  display: {
    dateInput: 'DD/MM/YYYY HH:mm',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY'
  }
};

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    MessageFilterComponent,
    ManageBackendsComponent,
    MessageLogComponent,
    UserComponent,
    ErrorLogComponent,
    AlertComponent,
    CurrentPModeComponent,
    PModeArchiveComponent,
    BaseTruststoreComponent,
    TLSTruststoreComponent,
    TruststoreComponent,
    KeystoreComponent,
    PmodeUploadComponent,
    PmodeViewComponent,
    YesNoDialogComponent,
    OkDialogComponent,
    JmsComponent,
    RowLimiterComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    DomibusDatePipe,
    DomibusFutureDatePipe,
    CapitalizeFirstPipe,
    SanitizeHtmlPipe,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    MessagelogDetailsComponent,
    ErrorlogDetailsComponent,
    EditUserComponent,
    TruststoreDialogComponent,
    TrustStoreUploadComponent,
    ColumnPickerComponent,
    CertificateUploadComponent,
    PageHelperComponent,
    ActionDirtyDialogComponent,
    AuditComponent,
    PartyComponent,
    PartyDetailsComponent,
    PageHeaderComponent,
    DomainSelectorComponent,
    AlertsComponent,
    PluginUserComponent,
    EditPluginUserFormBaseComponent,
    EditBasicPluginUserFormComponent,
    EditCertificatePluginUserFormComponent,
    PartyIdentifierDetailsComponent,
    LoggingComponent,
    ChangePasswordComponent,
    LogoutAuthExtProviderComponent,
    NotAuthorizedComponent,
    PropertiesComponent,
    GridHeaderComponent,
    FilterAreaFooterComponent,
    PageFooterComponent,
    PageGridComponent,
    UserCrossFieldValidatorsDirective,
    EditPopupFooterComponent,
    EditPopupBaseComponent,
    ConnectionsComponent,
    ConnectionDetailsComponent,
    SessionExpiredDialogComponent,
    AddNestedPropertyDialogComponent,
    DomainsComponent,
    DataTablePagerComponent
  ],
  imports: [
    NgxMatDatetimePickerModule,
    MatDialogModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgxDatatableModule,
    MatButtonModule,
    MatDialogModule,
    MatTooltipModule,
    MatMenuModule,
    MatInputModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatSelectModule,
    routing,
    ReactiveFormsModule,
    SharedModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatButtonToggleModule,
    HttpClientModule,
    MatCardModule,
    CommonModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDatepickerModule,
    NgxMatDatetimePickerModule,
    NgxMatMomentModule
  ],
  providers: [
    MatDatepickerModule,
    NgxMatMomentModule,
    NgxMatDatetimePickerModule,
    AuthenticatedAuthorizedGuard,
    DirtyGuard,
    DefaultPasswordGuard,
    AuthExternalProviderGuard,
    AuthInternalProviderGuard,
    RedirectHomeGuard,
    LandingPageGuard,
    HttpEventService,
    SecurityService,
    SessionService,
    SecurityEventService,
    DomainService,
    DomibusInfoService,
    AlertService,
    {provide: HTTP_INTERCEPTORS, useClass: ExtendedHttpInterceptor, multi: true},
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler,
    },
    UserService,
    UserValidatorService,
    PluginUserValidatorService,
    DialogsService,
    PropertiesService,
    HelperService,
    FileUploadValidatorService,
    ApplicationContextService,
    DatePipe,
    DecimalPipe,
    DateService,
    {provide: NGX_MAT_DATE_FORMATS, useValue: CUSTOM_MOMENT_FORMATS}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}


