import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { AlertService } from '../common/alert/alert.service';
import { PropertiesService, PropertyListModel, PropertyModel } from './support/properties.service';
import { SecurityService } from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import { ServerPageableListMixin } from '../common/mixins/pageable-list.mixin';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import { HttpClient } from '@angular/common/http';
import { ApplicationContextService } from '../common/application-context.service';
import { ComponentName } from '../common/component-name-decorator';
import { DialogsService } from '../common/dialogs/dialogs.service';
import { AddNestedPropertyDialogComponent } from './support/add-nested-property-dialog/add-nested-property-dialog.component';
import { ServerSortableListMixin } from '../common/mixins/sortable-list.mixin';

@Component({
  templateUrl: 'properties.component.html',
  styleUrls: ['properties.component.css'],
  providers: [PropertiesService]
})
@ComponentName('Domibus Properties')
export class PropertiesComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin, ServerSortableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  showGlobalPropertiesControl: boolean;

  @ViewChild('propertyValueTpl') propertyValueTpl: TemplateRef<any>;
  private inLostFocus: boolean;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private propertiesService: PropertiesService,
    private alertService: AlertService, private securityService: SecurityService, private changeDetector: ChangeDetectorRef,
    private dialogsService: DialogsService) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    super.filter = { propertyName: '', showDomain: true, type: null, module: null, value: null, writable: true };
    this.showGlobalPropertiesControl = this.securityService.isCurrentUserSuperAdmin();

    this.propertiesService.loadPropertyTypes();
    this.filterData();
  }

  protected get GETUrl(): string {
    return PropertiesService.PROPERTIES_URL;
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Property Name',
        prop: 'name',
        showInitially: true,
        width: 350,
        minWidth: 340
      },
      {
        name: 'Type',
        prop: 'type',
        showInitially: true,
        width: 140,
        minWidth: 130
      },
      // {
      //   name: 'Description',
      //   prop: 'description',
      //   width: 100,
      //   minWidth: 100,
      //   sortable: false
      // },
      {
        name: 'Module',
        prop: 'module',
        width: 100,
        minWidth: 90,
      },
      // {
      //   name: 'Section',
      //   prop: 'section',
      //   width: 100,
      //   minWidth: 90,
      //   sortable: false
      // },
      {
        name: 'Usage',
        prop: 'usageText',
        showInitially: true,
        width: 200,
        minWidth: 190
      },
      {
        name: 'With Fallback',
        prop: 'withFallback',
        width: 80,
        minWidth: 70,
        sortable: false
      },
      {
        name: 'Is Writable',
        prop: 'writable',
        width: 80,
        minWidth: 70,
        sortable: false
      },
      {
        name: 'Is Encrypted',
        prop: 'encrypted',
        width: 80,
        minWidth: 70,
        sortable: false
      },
      {
        name: 'Is Composable',
        prop: 'composable',
        width: 80,
        minWidth: 70,
        sortable: false
      },
      {
        cellTemplate: this.propertyValueTpl,
        name: 'Property Value',
        prop: 'value',
        showInitially: true,
        width: 350,
        minWidth: 340,
        sortable: false
      },

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => col.showInitially);
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  public setServerResults(result: PropertyListModel) {
    super.count = result.count;
    let rows = result.items;
    rows.forEach(row => row.originalValue = row.value);
    super.rows = rows;
  }

  onPropertyValueFocus(row: PropertyModel) {
    if (row.timeoutId) {
      clearTimeout(row.timeoutId);
      row.timeoutId = null;
      return;
    }
    row.currentValueSet = true;
    row.currentValue = row.value;
    this.alertService.clearAlert();
  }

  onPropertyValueBlur(row: PropertyModel) {
    row.timeoutId = window.setTimeout(() => {
      this.revertProperty(row);
      row.timeoutId = null;
    }, 500);
  }

  canUpdate(row: PropertyModel): boolean {
    if (row && !row.currentValueSet) {
      return false;
    }
    return row && row.currentValue != row.value;
  }

  async updateProperty(row: PropertyModel) {
    try {
      row.oldValue = row.currentValue;
      row.currentValue = row.value;
      await this.propertiesService.updateProperty(row, this.filter.showDomain);
      row.originalValue = row.usedValue;
      this.alertService.success('Successfully updated the property ' + row.name);
    } catch (ex) {
      row.currentValue = row.oldValue;
      this.revertProperty(row);
      if (!ex.handled) {
        this.alertService.exception('Could not update property: ', ex);
      }
    }
  }

  revertProperty(row: PropertyModel) {
    row.value = row.currentValue;
  }

  get csvUrl(): string {
    return PropertiesService.PROPERTIES_URL + '/csv' + '?' + this.createAndSetParameters();
  }

  canWriteProperty(row: PropertyModel) {
    return row.writable && !row.composable;
  }

  async addNewNestedProperty(property: PropertyModel) {
    const data = await this.dialogsService.openAndThen(AddNestedPropertyDialogComponent, { data: property });
    if (!data) {
      return;
    }
    let propertyName = property.name + '.' + data.propertySuffix;
    try {
      if (await this.propertyExists(propertyName)) {
        this.alertService.error(`There is already a property with the name ${propertyName}. Please update it instead if this is the intention`);
        return;
      }

      const newProp: PropertyModel = JSON.parse(JSON.stringify(property));
      newProp.name = propertyName;
      newProp.value = data.propertyValue;
      newProp.composable = false;
      await this.propertiesService.updateProperty(newProp, this.filter.showDomain);

      super.rows = [...this.rows, newProp];
      super.count = this.rows.length;
    } catch (e) {
      this.alertService.exception(`Error trying to add a property with the name ${propertyName}.`, e);
    }
  }

  async propertyExists(propertyName: string): Promise<boolean> {
    try {
      const existing = await this.propertiesService.getProperty(propertyName);
      return existing != null;
    } catch (ex) {
      return false;
    }
  }

  syncValues(row: PropertyModel) {
    row.value = row.usedValue;
    this.updateProperty(row);
  }

  async retrievePassword(row) {
    console.log('Retrieving password for property:', row.name, row);

    const keyPair = await window.crypto.subtle.generateKey(
      {
        name: "RSA-OAEP",
        modulusLength: 2048, // can be 1024, 2048, or 4096
        publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
        hash: "SHA-256", // can be "SHA-1", "SHA-256", "SHA-384", or "SHA-512"
      },
      true, // whether the key is extractable (i.e. can be used in exportKey)
      ["encrypt", "decrypt"] // can be any combination of "encrypt", "decrypt", "wrapKey", or "unwrapKey"
    )

    const spkiArrayBuffer = await window.crypto.subtle.exportKey('spki', keyPair.publicKey);
    const publicKeyPem = arrayBufferToPem(spkiArrayBuffer);
    console.log('Public key PEM:', publicKeyPem);


    const propName = row.name;
    const response = await this.http.post<PropertyModel>(PropertiesService.PROPERTIES_URL + '/' + propName + '/password', {
      propName,
      publicKeyPem
    }).toPromise();

    console.log('Encrypted property value:', response);

    // Convert the encrypted property value from Base64 to an ArrayBuffer
    const encryptedValueArrayBuffer = base64ToArrayBuffer(response);
    console.log('Encrypted property value ArrayBuffer:', encryptedValueArrayBuffer);

    console.log('Private key:', keyPair.privateKey);

    // Decrypt the encrypted property value
    const decryptedValueArrayBuffer = await window.crypto.subtle.decrypt(
      {
        name: "RSA-OAEP"
      },
      keyPair.privateKey, // use the private key for decryption
      encryptedValueArrayBuffer
    );

    console.log('Decrypted property value ArrayBuffer:', decryptedValueArrayBuffer);

    // Convert the decrypted ArrayBuffer to a string
    const decryptedValue = new TextDecoder().decode(decryptedValueArrayBuffer);
    console.log('Decrypted property value:', decryptedValue);

  }

}

function arrayBufferToBase64(buffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary);
}


function base64ToArrayBuffer(base64) {
  const binaryString = window.atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}

function arrayBufferToPem(buffer: ArrayBuffer): string {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  const base64 = window.btoa(binary);
  return `-----BEGIN PUBLIC KEY-----\n${base64}\n-----END PUBLIC KEY-----`;
}
