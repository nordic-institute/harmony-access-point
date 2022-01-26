/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 * 
 * This file is part of the "DSS - Digital Signature Services" project.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.spi;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.RoleOfPspOid;
import eu.europa.esig.dss.enumerations.SemanticsIdentifier;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.model.QCLimitValue;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.X500PrincipalHelper;
import eu.europa.esig.dss.spi.x509.*;
import eu.europa.esig.dss.utils.Utils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.esf.RevocationValues;
import org.bouncycastle.asn1.ess.OtherCertID;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.DirectoryString;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.qualified.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.*;

import static eu.europa.esig.dss.spi.OID.*;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.*;

/**
 * Utility class that contains some ASN1 related method.
 *
 */
public final class DSSASN1Utils {

	private static final Logger LOG = LoggerFactory.getLogger(DSSASN1Utils.class);

	/** Contains a list of all CAdES timestamp OIDs */
	private static List<ASN1ObjectIdentifier> timestampOids;

	static {
		Security.addProvider(DSSSecurityProvider.getSecurityProvider());

		timestampOids = new ArrayList<>();
		timestampOids.add(id_aa_ets_contentTimestamp);
		timestampOids.add(id_aa_ets_archiveTimestampV2);
		timestampOids.add(id_aa_ets_archiveTimestampV3);
		timestampOids.add(id_aa_ets_certCRLTimestamp);
		timestampOids.add(id_aa_ets_escTimeStamp);
		timestampOids.add(id_aa_signatureTimeStampToken);
	}

	/** The QC type statement OID */
	private static final String QC_TYPE_STATEMENT_OID = "0.4.0.1862.1.6";

	/** The QC legislation OID */
	private static final String QC_LEGISLATION_OID = "0.4.0.1862.1.7";

	/**
	 * This class is an utility class and cannot be instantiated.
	 */
	private DSSASN1Utils() {
	}

	/**
	 * This method returns {@code T extends ASN1Primitive} created from array of bytes. The {@code IOException} is
	 * transformed in {@code DSSException}.
	 *
	 * @param bytes
	 *            array of bytes to be transformed to {@code ASN1Primitive}
	 * @param <T>
	 *            the expected return type
	 * @return new {@code T extends ASN1Primitive}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ASN1Primitive> T toASN1Primitive(final byte[] bytes) {
		try {
			return (T) ASN1Primitive.fromByteArray(bytes);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method checks if a given {@code DEROctetString} is null.
	 *
	 * @param derOctetString
	 *            the {@code DEROctetString} to check
	 * @return true if the {@code DEROctetString} contains DERNull
	 */
	private static boolean isDEROctetStringNull(final DEROctetString derOctetString) {
		final byte[] derOctetStringBytes = derOctetString.getOctets();
		final ASN1Primitive asn1Null = toASN1Primitive(derOctetStringBytes);
		return DERNull.INSTANCE.equals(asn1Null);
	}

	/**
	 * This method returns DER encoded ASN1 attribute. The {@code IOException} is
	 * transformed in {@code DSSException}.
	 *
	 * @param asn1Encodable
	 *            asn1Encodable to be DER encoded
	 * @return array of bytes representing the DER encoded asn1Encodable
	 */
	public static byte[] getDEREncoded(ASN1Encodable asn1Encodable) {
		return getEncoded(asn1Encodable, ASN1Encoding.DER);
	}

	/**
	 * This method returns BER encoded ASN1 attribute. The {@code IOException} is
	 * transformed in {@code DSSException}.
	 *
	 * @param asn1Encodable
	 *            asn1Encodable to be BER encoded
	 * @return array of bytes representing the BER encoded asn1Encodable
	 */
	public static byte[] getBEREncoded(ASN1Encodable asn1Encodable) {
		return getEncoded(asn1Encodable, ASN1Encoding.BER);
	}

	/**
	 * This method returns encoded ASN1 attribute. The {@code IOException} is
	 * transformed in {@code DSSException}.
	 *
	 * @param asn1Encodable
	 *            asn1Encodable to be the given encoding
	 * @param encoding
	 *            the expected encoding
	 * @return array of bytes representing the encoded asn1Encodable
	 */
	private static byte[] getEncoded(ASN1Encodable asn1Encodable, String encoding) {
		try {
			return asn1Encodable.toASN1Primitive().getEncoded(encoding);
		} catch (IOException e) {
			throw new DSSException("Unable to encode to " + encoding, e);
		}
	}

	/**
	 * Gets the DER-encoded binaries of the {@code BasicOCSPResp}
	 *
	 * @param basicOCSPResp {@link BasicOCSPResp}
	 * @return DER-encoded binaries
	 */
	public static byte[] getEncoded(BasicOCSPResp basicOCSPResp) {
		try {
			BasicOCSPResponse basicOCSPResponse = BasicOCSPResponse.getInstance(basicOCSPResp.getEncoded());
			return getDEREncoded(basicOCSPResponse);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * Converts {@code ASN1GeneralizedTime} to {@code Date}
	 *
	 * @param asn1Date {@link ASN1GeneralizedTime}
	 * @return {@link Date}
	 */
	public static Date toDate(final ASN1GeneralizedTime asn1Date) {
		try {
			return asn1Date.getDate();
		} catch (ParseException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * Reads {@code ASN1OctetString} value and returns
	 *
	 * @param value {@link ASN1OctetString}
	 * @return {@link String}
	 */
	public static String toString(final ASN1OctetString value) {
		return new String(value.getOctets());
	}

	/**
	 * Returns an ASN.1 encoded bytes representing the {@code TimeStampToken}
	 *
	 * @param timeStampToken
	 *                       {@code TimeStampToken}
	 * @return the DER encoded {@code TimeStampToken}
	 */
	public static byte[] getEncoded(final TimeStampToken timeStampToken) {
		return getEncoded(timeStampToken.toCMSSignedData());
	}

	/**
	 * Returns an ASN.1 encoded bytes representing the {@code CMSSignedData}
	 *
	 * @param cmsSignedData
	 *                       {@code CMSSignedData}
	 * @return the binary of the {@code CMSSignedData} @ if the {@code
	 * CMSSignedData} encoding fails
	 */
	public static byte[] getEncoded(final CMSSignedData cmsSignedData) {
		try {
			return cmsSignedData.getEncoded();
		} catch (IOException e) {
			throw new DSSException("Unable to encode to DER", e);
		}
	}

	/**
	 * Gets the DER encoded binaries of {@code TimeStampToken}
	 *
	 * @param timeStampToken {@link TimeStampToken}
	 * @return DER encoded binaries
	 */
	public static byte[] getDEREncoded(final TimeStampToken timeStampToken) {
		return getDEREncoded(timeStampToken.toCMSSignedData());
	}

	/**
	 * Returns the ASN.1 encoded representation of {@code CMSSignedData}.
	 *
	 * @param data
	 *             the CMSSignedData to be encoded
	 * @return the DER encoded CMSSignedData
	 */
	public static byte[] getDEREncoded(final CMSSignedData data) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			final ASN1OutputStream asn1OutputStream = ASN1OutputStream.create(baos, ASN1Encoding.DER);
			asn1OutputStream.writeObject(data.toASN1Structure());
			asn1OutputStream.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new DSSException("Unable to encode to DER", e);
		}
	}

	/**
	 * Returns the ASN.1 encoded representation of {@code TimestampBinary}.
	 *
	 * @param timestampBinary
	 *             the {@link TimestampBinary} to be encoded
	 * @return the DER encoded timestampBinary
	 */
	public static byte[] getDEREncoded(final TimestampBinary timestampBinary) {
		return getDEREncoded(timestampBinary.getBytes());
	}

	/**
	 * Returns the ASN.1 encoded representation of {@code byte} array.
	 *
	 * @param bytes
	 *             the binary array to encode
	 * @return the DER encoded bytes
	 */
	public static byte[] getDEREncoded(final byte[] bytes) {
		try {
			return getDEREncoded(ASN1Primitive.fromByteArray(bytes));
		} catch (IOException e) {
			throw new DSSException("Unable to encode to DER", e);
		}
	}

	/**
	 * This method returns the {@code ASN1Sequence} encapsulated in
	 * {@code DEROctetString}. The {@code DEROctetString} is represented as
	 * {@code byte} array.
	 *
	 * @param bytes
	 *              {@code byte} representation of {@code DEROctetString}
	 * @return encapsulated {@code ASN1Sequence} @ in case of a decoding problem
	 */
	public static ASN1Sequence getAsn1SequenceFromDerOctetString(byte[] bytes) {
		return getASN1Sequence(getDEROctetStringContent(bytes));
	}

	private static byte[] getDEROctetStringContent(byte[] bytes) {
		try (ASN1InputStream input = new ASN1InputStream(bytes)) {
			final DEROctetString s = (DEROctetString) input.readObject();
			return s.getOctets();
		} catch (IOException e) {
			throw new DSSException("Unable to retrieve the DEROctetString content", e);
		}
	}

	private static ASN1Sequence getASN1Sequence(byte[] bytes) {
		try (ASN1InputStream input = new ASN1InputStream(bytes)) {
			return (ASN1Sequence) input.readObject();
		} catch (IOException e) {
			throw new DSSException("Unable to retrieve the ASN1Sequence", e);
		}
	}

	/**
	 * This method computes the digest of an ASN1 signature policy (used in CAdES)
	 *
	 * TS 101 733 5.8.1 : If the signature policy is defined using ASN.1, then the hash is calculated on the value
	 * without the outer type and length
	 * fields, and the hashing algorithm shall be as specified in the field sigPolicyHash.
	 * 
	 * @param digestAlgorithm
	 *            the digest algorithm to be used
	 * @param policyBytes
	 *            the ASN.1 policy content
	 * @return the expected digest value
	 */
	public static byte[] getAsn1SignaturePolicyDigest(DigestAlgorithm digestAlgorithm, byte[] policyBytes) {
		ASN1Sequence asn1Seq = toASN1Primitive(policyBytes);

		ASN1Sequence signPolicyHashAlgObject = (ASN1Sequence) asn1Seq.getObjectAt(0);
		AlgorithmIdentifier signPolicyHashAlgIdentifier = AlgorithmIdentifier.getInstance(signPolicyHashAlgObject);
		ASN1Sequence signPolicyInfo = (ASN1Sequence) asn1Seq.getObjectAt(1);

		byte[] hashAlgorithmDEREncoded = getDEREncoded(signPolicyHashAlgIdentifier);
		byte[] signPolicyInfoDEREncoded = getDEREncoded(signPolicyInfo);
		return DSSUtils.digest(digestAlgorithm, hashAlgorithmDEREncoded, signPolicyInfoDEREncoded);
	}

	/**
	 * Gets the ASN.1 algorithm identifier structure corresponding to the algorithm 
	 * found in the provided Timestamp Hash Index Table, if such algorithm is present
	 *
	 * @param atsHashIndexValue
	 *            ats-hash-index table from a timestamp
	 * @return the ASN.1 algorithm identifier structure
	 */
	public static AlgorithmIdentifier getAlgorithmIdentifier(final ASN1Sequence atsHashIndexValue) {
		if (atsHashIndexValue != null && atsHashIndexValue.size() > 3) {
			final int algorithmIndex = 0;
			final ASN1Encodable asn1Encodable = atsHashIndexValue.getObjectAt(algorithmIndex);
			
			if (asn1Encodable instanceof ASN1Sequence) {
				final ASN1Sequence asn1Sequence = (ASN1Sequence) asn1Encodable;
				return AlgorithmIdentifier.getInstance(asn1Sequence);
			} else if (asn1Encodable instanceof ASN1ObjectIdentifier) {
				// TODO (16/11/2014): The relevance and usefulness of the test case must be checked (do the signatures
				// like this exist?)
				ASN1ObjectIdentifier derObjectIdentifier = ASN1ObjectIdentifier.getInstance(asn1Encodable);
				return new AlgorithmIdentifier(derObjectIdentifier);
			}
		}
		return null;
	}

	/**
	 * Gets the ASN.1 algorithm identifier structure corresponding to a digest algorithm
	 *
	 * @param digestAlgorithm
	 *            the digest algorithm to encode
	 * @return the ASN.1 algorithm identifier structure
	 */
	public static AlgorithmIdentifier getAlgorithmIdentifier(DigestAlgorithm digestAlgorithm) {

		/*
		 * The recommendation (cf. RFC 3380 section 2.1) is to omit the parameter for SHA-1, but some implementations
		 * still expect a
		 * NULL there. Therefore we always include a NULL parameter even with SHA-1, despite the recommendation, because
		 * the RFC
		 * states that implementations SHOULD support it as well anyway
		 */
		final ASN1ObjectIdentifier asn1ObjectIdentifier = new ASN1ObjectIdentifier(digestAlgorithm.getOid());
		return new AlgorithmIdentifier(asn1ObjectIdentifier, DERNull.INSTANCE);
	}

	/**
	 * Extract the Unsigned Attribute Archive Timestamp Cert Hash Index from a timestampToken
	 *
	 * @param atsHashIndexValue {@link ASN1Sequence}
	 * @return {@link ASN1Sequence}
	 */
	public static ASN1Sequence getCertificatesHashIndex(final ASN1Sequence atsHashIndexValue) {
		if (atsHashIndexValue != null) {
			int certificateIndex = 0;
			if (atsHashIndexValue.size() > 3) {
				certificateIndex++;
			}
			return (ASN1Sequence) atsHashIndexValue.getObjectAt(certificateIndex).toASN1Primitive();
		}
		return null;
	}

	/**
	 * Extract the Unsigned Attribute Archive Timestamp Crl Hash Index from a timestampToken
	 *
	 * @param atsHashIndexValue {@link ASN1Sequence}
	 * @return {@link ASN1Sequence}
	 */
	public static ASN1Sequence getCRLHashIndex(final ASN1Sequence atsHashIndexValue) {
		if (atsHashIndexValue != null) {
			int crlIndex = 1;
			if (atsHashIndexValue.size() > 3) {
				crlIndex++;
			}
			return (ASN1Sequence) atsHashIndexValue.getObjectAt(crlIndex).toASN1Primitive();
		}
		return null;
	}

	/**
	 * Extract the Unsigned Attribute Archive Timestamp Attribute Hash Index from a timestampToken
	 *
	 * @param atsHashIndexValue {@link ASN1Sequence}
	 * @return {@link ASN1Sequence}
	 */
	public static ASN1Sequence getUnsignedAttributesHashIndex(final ASN1Sequence atsHashIndexValue) {
		if (atsHashIndexValue != null) {
			int unsignedAttributesIndex = 2;
			if (atsHashIndexValue.size() > 3) {
				unsignedAttributesIndex++;
			}
			return (ASN1Sequence) atsHashIndexValue.getObjectAt(unsignedAttributesIndex).toASN1Primitive();
		}
		return null;
	}

	/**
	 * Returns list of {@code DEROctetString} from an {@code ASN1Sequence}
	 * Useful when needed to get a list of hash values
	 * 
	 * @param asn1Sequence {@link ASN1Sequence} to get list from
	 * @return list of {@link DEROctetString}s
	 */
	@SuppressWarnings("unchecked")
	public static List<DEROctetString> getDEROctetStrings(final ASN1Sequence asn1Sequence) {
		final List<DEROctetString> derOctetStrings = new ArrayList<>();
		if (asn1Sequence != null) {
			derOctetStrings.addAll(Collections.list(asn1Sequence.getObjects()));
		}
		return derOctetStrings;
	}

	/**
	 * Indicates if the revocation data should be checked for an OCSP signing certificate.<br>
	 * http://www.ietf.org/rfc/rfc2560.txt?number=2560<br>
	 * A CA may specify that an OCSP client can trust a responder for the lifetime of the responder's certificate. The
	 * CA does so by including the extension id-pkix-ocsp-nocheck. This SHOULD be a non-critical extension. The value of
	 * the extension should be NULL.
	 *
	 * @param token
	 *            the certificate to be checked
	 * @return true if the certificate has the id_pkix_ocsp_nocheck extension
	 */
	public static boolean hasIdPkixOcspNoCheckExtension(CertificateToken token) {
		final byte[] extensionValue = token.getCertificate().getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId());
		if (extensionValue != null) {
			try {
				final ASN1Primitive derObject = toASN1Primitive(extensionValue);
				if (derObject instanceof DEROctetString) {
					return isDEROctetStringNull((DEROctetString) derObject);
				}
			} catch (Exception e) {
				LOG.debug("Exception when processing 'id_pkix_ocsp_no_check'", e);
			}
		}
		return false;
	}

	/**
	 * Retrieves a list of {@code CertificatePolicy}s from a certificate token
	 *
	 * @param certToken {@link CertificateToken}
	 * @return a list of {@code CertificatePolicy}s
	 */
	public static List<CertificatePolicy> getCertificatePolicies(final CertificateToken certToken) {
		List<CertificatePolicy> certificatePolicies = new ArrayList<>();
		final byte[] certificatePoliciesBinaries = certToken.getCertificate().getExtensionValue(Extension.certificatePolicies.getId());
		if (Utils.isArrayNotEmpty(certificatePoliciesBinaries)) {
			try {
				ASN1Sequence seq = getAsn1SequenceFromDerOctetString(certificatePoliciesBinaries);
				for (int ii = 0; ii < seq.size(); ii++) {
					CertificatePolicy cp = new CertificatePolicy();
					final PolicyInformation policyInfo = PolicyInformation.getInstance(seq.getObjectAt(ii));
					cp.setOid(policyInfo.getPolicyIdentifier().getId());
					ASN1Sequence policyQualifiersSeq = policyInfo.getPolicyQualifiers();
					if (policyQualifiersSeq != null) {
						for (int jj = 0; jj < policyQualifiersSeq.size(); jj++) {
							PolicyQualifierInfo pqi = PolicyQualifierInfo.getInstance(policyQualifiersSeq.getObjectAt(jj));
							if (PolicyQualifierId.id_qt_cps.equals(pqi.getPolicyQualifierId())) {
								cp.setCpsUrl(getString(pqi.getQualifier()));
							}
						}
					}
					certificatePolicies.add(cp);
				}
			} catch (Exception e) {
				LOG.warn("Unable to parse the certificatePolicies extension '{}' : {}", Utils.toBase64(certificatePoliciesBinaries), e.getMessage(), e);
			}
		}
		return certificatePolicies;
	}

	/**
	 * Get the list of all QCStatement Ids that are present in the certificate.
	 * (As per ETSI EN 319 412-5 V2.1.1)
	 * 
	 * @param certToken
	 *            the certificate
	 * @return the list of QC Statements oids
	 */
	public static List<String> getQCStatementsIdList(final CertificateToken certToken) {
		final List<String> extensionIdList = new ArrayList<>();
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				// Sequence of QCStatement
				for (int ii = 0; ii < seq.size(); ii++) {
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(ii));
					extensionIdList.add(statement.getStatementId().getId());
				}
			} catch (Exception e) {
				LOG.warn("Unable to parse the qCStatements extension '{}' : {}", Utils.toBase64(qcStatement), e.getMessage(), e);
			}
		}
		return extensionIdList;
	}

	/**
	 * Get the list of all QCType Ids that are present in the certificate.
	 * (As per ETSI EN 319 412-5 V2.1.1)
	 * 
	 * @param certToken
	 *            the certificate
	 * @return the list of QCTypes oids
	 */
	public static List<String> getQCTypesIdList(final CertificateToken certToken) {
		final List<String> qcTypesIdList = new ArrayList<>();
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				// Sequence of QCStatement
				for (int ii = 0; ii < seq.size(); ii++) {
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(ii));
					if (QC_TYPE_STATEMENT_OID.equals(statement.getStatementId().getId())) {
						final ASN1Encodable qcTypeInfo1 = statement.getStatementInfo();
						if (qcTypeInfo1 instanceof ASN1Sequence) {
							final ASN1Sequence qcTypeInfo = (ASN1Sequence) qcTypeInfo1;
							for (int jj = 0; jj < qcTypeInfo.size(); jj++) {
								final ASN1Encodable e1 = qcTypeInfo.getObjectAt(jj);
								if (e1 instanceof ASN1ObjectIdentifier) {
									final ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e1;
									qcTypesIdList.add(oid.getId());
								} else {
									LOG.warn("ASN1Sequence in QcTypes does not contain ASN1ObjectIdentifer, but {}",
											e1.getClass().getName());
								}
							}
						} else {
							LOG.warn("QcTypes not an ASN1Sequence, but {}", qcTypeInfo1.getClass().getName());
						}
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to parse the qCStatements extension '{}' : {}", Utils.toBase64(qcStatement), e.getMessage(), e);
			}
		}

		return qcTypesIdList;
	}

	/**
	 * Retrieves a list of QC Legislation statements from the given certificate token
	 *
	 * @param certToken {@link CertificateToken}
	 * @return a list of QC Legislation statements
	 */
	public static List<String> getQCLegislations(CertificateToken certToken) {
		final List<String> result = new ArrayList<>();
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				// Sequence of QCStatement
				for (int ii = 0; ii < seq.size(); ii++) {
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(ii));
					if (QC_LEGISLATION_OID.equals(statement.getStatementId().getId())) {
						ASN1Sequence sequenceLegislation = ASN1Sequence.getInstance(statement.getStatementInfo());
						for (int jj = 0; jj < sequenceLegislation.size(); jj++) {
							result.add(getString(sequenceLegislation.getObjectAt(jj)));
						}
						
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to parse the qCStatements extension '{}' : {}", Utils.toBase64(qcStatement), e.getMessage(), e);
			}
		}
		return result;
	}

	/**
	 * This method returns the Subject Key Identifier (SKI) bytes from the
	 * certificate extension (SHA-1 of the public key of the current certificate).
	 *
	 * @param certificateToken
	 *                         the {@code CertificateToken}
	 * @return ski bytes from the given certificate or null if missing
	 */
	public static byte[] getSki(final CertificateToken certificateToken) {
		return getSki(certificateToken, false);
	}

	/**
	 * This method returns SKI bytes from certificate.
	 *
	 * @param certificateToken
	 *            {@code CertificateToken}
	 * @param computeIfMissing
	 *            if the extension is missing and computeIfMissing = true, it will compute the SKI value from the Public
	 *            Key
	 * @return ski bytes from the given certificate
	 */
	public static byte[] getSki(final CertificateToken certificateToken, boolean computeIfMissing) {
		try {
			byte[] extensionValue = certificateToken.getCertificate().getExtensionValue(Extension.subjectKeyIdentifier.getId());
			if (Utils.isArrayNotEmpty(extensionValue)) {
				ASN1Primitive extension = JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
				SubjectKeyIdentifier skiBC = SubjectKeyIdentifier.getInstance(extension);
				return skiBC.getKeyIdentifier();
			} else if (computeIfMissing) {
				// If extension not present, we compute it from the certificate public key
				return computeSkiFromCert(certificateToken);
			}
			return null;
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method returns authority key identifier as binaries from the certificate
	 * extension (SHA-1 of the public key of the issuer certificate).
	 *
	 * @param certificateToken
	 *                         the {@code CertificateToken}
	 * @return authority key identifier bytes from the given certificate (can be
	 *         null if the certificate is self signed)
	 */
	public static byte[] getAuthorityKeyIdentifier(CertificateToken certificateToken) {
		byte[] extensionValue = certificateToken.getCertificate().getExtensionValue(Extension.authorityKeyIdentifier.getId());
		if (Utils.isArrayNotEmpty(extensionValue)) {
			try {
				ASN1Primitive extension = JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
				AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(extension);
				return aki.getKeyIdentifier();
			} catch (IOException e) {
				throw new DSSException("Unable to parse the authorityKeyIdentifier extension", e);
			}
		}
		return null;
	}

	/**
	 * Computes SHA-1 hash of the {@code certificateToken}'s public key
	 * 
	 * @param certificateToken
	 *                         {@link CertificateToken} to compute digest for
	 * @return byte array of public key's SHA-1 hash
	 */
	public static byte[] computeSkiFromCert(final CertificateToken certificateToken) {
		return computeSkiFromCertPublicKey(certificateToken.getPublicKey());
	}

	/**
	 * Computes SHA-1 hash of the given {@code publicKey}'s
	 * @param publicKey {@link PublicKey} to compute digest for
	 * @return byte array of public key's SHA-1 hash
	 */
	public static byte[] computeSkiFromCertPublicKey(final PublicKey publicKey) {
		try {
			DLSequence seq = (DLSequence) ASN1Primitive.fromByteArray(publicKey.getEncoded());
			DERBitString item = (DERBitString) seq.getObjectAt(1);
			return DSSUtils.digest(DigestAlgorithm.SHA1, item.getOctets());
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}
	
	/**
	 * Checks if the provided ski matches to a ski computed from a certificateToken's public key
	 * 
	 * @param ski a byte array representing ski value (SHA-1 of the public key)
	 * @param certificateToken {@link CertificateToken} to check
	 * @return TRUE if the SKI equals, FALSE otherwise
	 */
	public static boolean isSkiEqual(final byte[] ski, final CertificateToken certificateToken) {
		byte[] certSki = computeSkiFromCert(certificateToken);
        return Arrays.equals(certSki, ski);
	}

	/**
	 * Gives back the CA URIs meta-data found within the given certificate.
	 *
	 * @param certificate
	 *            the certificate token.
	 * @return a list of CA URIs, or empty list if the extension is not present.
	 */
	public static List<String> getCAAccessLocations(final CertificateToken certificate) {
		return getAccessLocations(certificate, X509ObjectIdentifiers.id_ad_caIssuers);
	}

	/**
	 * Gives back the OCSP URIs meta-data found within the given X509 cert.
	 *
	 * @param certificate
	 *            the cert token.
	 * @return a list of OCSP URIs, or empty list if the extension is not present.
	 */
	public static List<String> getOCSPAccessLocations(final CertificateToken certificate) {
		return getAccessLocations(certificate, X509ObjectIdentifiers.id_ad_ocsp);
	}

	private static List<String> getAccessLocations(final CertificateToken certificate, ASN1ObjectIdentifier aiaType) {
		List<String> locationsUrls = new ArrayList<>();
		final byte[] authInfoAccessExtensionValue = certificate.getCertificate().getExtensionValue(Extension.authorityInfoAccess.getId());
		if (null == authInfoAccessExtensionValue) {
			return locationsUrls;
		}

		try {
			ASN1Sequence asn1Sequence = DSSASN1Utils.getAsn1SequenceFromDerOctetString(authInfoAccessExtensionValue);
			if (asn1Sequence == null || asn1Sequence.size() == 0) {
				LOG.warn("Empty ASN1Sequence for AuthorityInformationAccess");
				return locationsUrls;
			}
			AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(asn1Sequence);
			AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
			for (AccessDescription accessDescription : accessDescriptions) {
				if (aiaType.equals(accessDescription.getAccessMethod())) {
					GeneralName gn = accessDescription.getAccessLocation();
					String location = parseGn(gn);
					if (location != null) {
						locationsUrls.add(location);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Unable to parse authorityInfoAccess", e);
		}
		return locationsUrls;
	}

	/**
	 * Gives back the {@code List} of CRL URI meta-data found within the given X509 certificate.
	 *
	 * @param certificateToken
	 *            the cert token certificate
	 * @return the {@code List} of CRL URI, or empty list if the extension is not present
	 */
	public static List<String> getCrlUrls(final CertificateToken certificateToken) {
		final List<String> urls = new ArrayList<>();

		final byte[] crlDistributionPointsBytes = certificateToken.getCertificate().getExtensionValue(Extension.cRLDistributionPoints.getId());
		if (crlDistributionPointsBytes != null) {
			try {
				final ASN1Sequence asn1Sequence = DSSASN1Utils.getAsn1SequenceFromDerOctetString(crlDistributionPointsBytes);
				final CRLDistPoint distPoint = CRLDistPoint.getInstance(asn1Sequence);
				final DistributionPoint[] distributionPoints = distPoint.getDistributionPoints();
				for (final DistributionPoint distributionPoint : distributionPoints) {

					final DistributionPointName distributionPointName = distributionPoint.getDistributionPoint();
					if (DistributionPointName.FULL_NAME != distributionPointName.getType()) {
						continue;
					}
					final GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
					final GeneralName[] names = generalNames.getNames();
					for (final GeneralName name : names) {
						String location = parseGn(name);
						if (location != null) {
							urls.add(location);
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Unable to parse cRLDistributionPoints", e);
			}
		}

		return urls;
	}

	private static String parseGn(GeneralName gn) {
		try {
			if (GeneralName.uniformResourceIdentifier == gn.getTagNo()) {
				ASN1String str = (ASN1String) ((DERTaggedObject) gn.toASN1Primitive()).getObject();
				return str.getString();
			}
		} catch (Exception e) {
			LOG.warn("Unable to parse GN '{}'", gn, e);
		}
		return null;
	}

	/**
	 * Indicates that a X509Certificates corresponding private key is used by an authority to sign OCSP-Responses.<br>
	 * http://www.ietf.org/rfc/rfc3280.txt <br>
	 * http://tools.ietf.org/pdf/rfc6960.pdf 4.2.2.2<br>
	 * {iso(1) identified-organization(3) dod(6) internet(1) security(5) mechanisms(5) pkix(7) keyPurpose(3)
	 * ocspSigning(9)}<br>
	 * OID: 1.3.6.1.5.5.7.3.9
	 *
	 * @param certToken
	 *            the certificate token
	 * @return true if the certificate has the id_kp_OCSPSigning ExtendedKeyUsage
	 */
	public static boolean isOCSPSigning(CertificateToken certToken) {
		return isExtendedKeyUsagePresent(certToken, KeyPurposeId.id_kp_OCSPSigning.toOID());
	}

	/**
	 * Checks if the keyUsage with {@code oid} is present in the certificate token
	 *
	 * @param certToken {@link CertificateToken}
	 * @param oid {@link ASN1ObjectIdentifier}
	 * @return TRUE if the certificate token contains a keyUsage with the given OID, FALSE otherwise
	 */
	public static boolean isExtendedKeyUsagePresent(CertificateToken certToken, ASN1ObjectIdentifier oid) {
		try {
			List<String> keyPurposes = certToken.getCertificate().getExtendedKeyUsage();
			if ((keyPurposes != null) && keyPurposes.contains(oid.getId())) {
				return true;
			}
		} catch (CertificateParsingException e) {
			LOG.error("Unable to retrieve ExtendedKeyUsage from certificate", e);
		}
		return false;
	}

	/**
	 * Returns a {@code X509CertificateHolder} encapsulating the given {@code X509Certificate}.
	 * 
	 * @param certToken
	 *            the certificate to be encapsulated
	 * @return a X509CertificateHolder holding this certificate
	 */
	public static X509CertificateHolder getX509CertificateHolder(CertificateToken certToken) {
		try {
			return new X509CertificateHolder(certToken.getEncoded());
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * Extract the certificate token from {@code X509CertificateHolder}
	 *
	 * @param x509CertificateHolder {@link X509CertificateHolder}
	 * @return {@link CertificateToken}
	 */
	public static CertificateToken getCertificate(final X509CertificateHolder x509CertificateHolder) {
		try {
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(DSSSecurityProvider.getSecurityProviderName());
			X509Certificate x509Certificate = converter.getCertificate(x509CertificateHolder);
			return new CertificateToken(x509Certificate);
		} catch (CertificateException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method transforms token's signerId into a {@code CertificateIdentifier}
	 * object
	 * 
	 * @param signerId {@link SignerId} to be transformed
	 * @return {@link CertificateIdentifier}
	 */
	public static CertificateIdentifier toIssuerSerialInfo(SignerId signerId) {
		X500Principal issuerX500Principal = toX500Principal(signerId.getIssuer());
		return toCertificateIdentifier(issuerX500Principal, signerId.getSerialNumber(), signerId.getSubjectKeyIdentifier());
	}
	
	/**
	 * Transforms x500Name to X500Principal
	 * 
	 * @param x500Name {@link X500Name}
	 * @return {@link X500Principal}
	 */
	public static X500Principal toX500Principal(X500Name x500Name) {
		if (x500Name == null) {
			return null;
		}
		try {
			return new X500Principal(x500Name.getEncoded());
		} catch (IOException e) {
			throw new DSSException(String.format("Cannot extract X500Principal! Reason : %s", e.getMessage()), e);
		}
	}
	
	/**
	 * This method transforms token's issuer and serial number information into a
	 * {@code CertificateIdentifier} object
	 * 
	 * @param issuerX500Principal {@link X500Principal} of the issuer
	 * @param serialNumber        {@link BigInteger} of the token
	 * @param ski                 a byte array representing a SubjectKeyIdentifier
	 *                            (SHA-1 digest of the public key)
	 * @return {@link CertificateIdentifier}
	 */
	public static CertificateIdentifier toCertificateIdentifier(final X500Principal issuerX500Principal, final BigInteger serialNumber, final byte[] ski) {
		CertificateIdentifier certificateIdentifier = new CertificateIdentifier();
		certificateIdentifier.setIssuerName(issuerX500Principal);
		certificateIdentifier.setSerialNumber(serialNumber);
		certificateIdentifier.setSki(ski);
		return certificateIdentifier;
	}

	/**
	 * This method returns a new IssuerSerial based on the certificate token
	 *
	 * @param certToken
	 *            the certificate token
	 * @return a IssuerSerial
	 */
	public static IssuerSerial getIssuerSerial(final CertificateToken certToken) {
		final X500Name issuerX500Name = getX509CertificateHolder(certToken).getIssuer();
		final GeneralName generalName = new GeneralName(issuerX500Name);
		final GeneralNames generalNames = new GeneralNames(generalName);
		final BigInteger serialNumber = certToken.getCertificate().getSerialNumber();
		return new IssuerSerial(generalNames, serialNumber);
	}

	/**
	 * This method compares two {@code X500Principal}s. {@code X500Principal.CANONICAL} and
	 * {@code X500Principal.RFC2253} forms are compared.
	 *
	 * @param firstX500Principal
	 *            the first X500Principal object to be compared
	 * @param secondX500Principal
	 *            the second X500Principal object to be compared
	 * @return true if the two parameters contain the same key/values
	 */
	public static boolean x500PrincipalAreEquals(final X500Principal firstX500Principal, final X500Principal secondX500Principal) {
		if ((firstX500Principal == null) || (secondX500Principal == null)) {
			return false;
		}
		if (firstX500Principal.equals(secondX500Principal)) {
			return true;
		}
		final Map<String, String> firstStringStringHashMap = DSSASN1Utils.get(firstX500Principal);
		final Map<String, String> secondStringStringHashMap = DSSASN1Utils.get(secondX500Principal);
		return firstStringStringHashMap.entrySet().containsAll(secondStringStringHashMap.entrySet());
	}

	/**
	 * Gets a map of X500 attribute names and the values
	 *
	 * @param x500Principal {@link X500Principal}
	 * @return a map of X500 attribute names and the values
	 */
	public static Map<String, String> get(final X500Principal x500Principal) {
		Map<String, String> treeMap = new HashMap<>();
		final byte[] encoded = x500Principal.getEncoded();
		final ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(encoded);
		final ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
		for (final ASN1Encodable asn1Encodable : asn1Encodables) {

			final DLSet dlSet = (DLSet) asn1Encodable;
			for (int ii = 0; ii < dlSet.size(); ii++) {

				final DLSequence dlSequence = (DLSequence) dlSet.getObjectAt(ii);
				if (dlSequence.size() != 2) {

					throw new DSSException("The DLSequence must contains exactly 2 elements.");
				}
				final ASN1Encodable asn1EncodableAttributeType = dlSequence.getObjectAt(0);
				final String stringAttributeType = getString(asn1EncodableAttributeType);
				final ASN1Encodable asn1EncodableAttributeValue = dlSequence.getObjectAt(1);
				final String stringAttributeValue = getString(asn1EncodableAttributeValue);
				treeMap.put(stringAttributeType, stringAttributeValue);
			}
		}
		return treeMap;
	}

	/**
	 * This method normalizes the X500Principal object
	 * 
	 * @param x500Principal
	 *            to be normalized
	 * @return {@code X500Principal} normalized
	 */
	public static X500Principal getNormalizedX500Principal(final X500Principal x500Principal) {
		final String utf8Name = DSSASN1Utils.getUtf8String(x500Principal);
		return new X500Principal(utf8Name);
	}

	/**
	 * Gets the UTF-8 distinguished names of {@code X500Principal}
	 *
	 * @param x500Principal {@link X500Principal}
	 * @return {@link String} utf-8
	 */
	public static String getUtf8String(final X500Principal x500Principal) {

		final byte[] encoded = x500Principal.getEncoded();
		final ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(encoded);
		final ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
		final StringBuilder stringBuilder = new StringBuilder();
		/**
		 * RFC 4514 LDAP: Distinguished Names
		 * 2.1. Converting the RDNSequence
		 *
		 * If the RDNSequence is an empty sequence, the result is the empty or
		 * zero-length string.
		 *
		 * Otherwise, the output consists of the string encodings of each
		 * RelativeDistinguishedName in the RDNSequence (according to Section
		 * 2.2), starting with the last element of the sequence and moving
		 * backwards toward the first.
		 * ...
		 */
		for (int ii = asn1Encodables.length - 1; ii >= 0; ii--) {

			final ASN1Encodable asn1Encodable = asn1Encodables[ii];

			final DLSet dlSet = (DLSet) asn1Encodable;
			for (int jj = 0; jj < dlSet.size(); jj++) {

				final DLSequence dlSequence = (DLSequence) dlSet.getObjectAt(jj);
				if (dlSequence.size() != 2) {

					throw new DSSException("The DLSequence must contains exactly 2 elements.");
				}
				final ASN1Encodable attributeType = dlSequence.getObjectAt(0);
				final ASN1Encodable attributeValue = dlSequence.getObjectAt(1);
				String string = getString(attributeValue);

				/**
				 * RFC 4514 LDAP: Distinguished Names
				 * ...
				 * Other characters may be escaped.
				 *
				 * Each octet of the character to be escaped is replaced by a backslash
				 * and two hex digits, which form a single octet in the code of the
				 * character. Alternatively, if and only if the character to be escaped
				 * is one of
				 *
				 * ' ', '"', '#', '+', ',', ';', '<', '=', '>', or '\'
				 * (U+0020, U+0022, U+0023, U+002B, U+002C, U+003B,
				 * U+003C, U+003D, U+003E, U+005C, respectively)
				 *
				 * it can be prefixed by a backslash ('\' U+005C).
				 */
				string = Rdn.escapeValue(string);
				if (stringBuilder.length() != 0) {
					stringBuilder.append(',');
				}
				stringBuilder.append(attributeType).append('=').append(string);
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Reads the value
	 *
	 * @param attributeValue {@link ASN1Encodable} to read
	 * @return {@link String} value
	 */
	public static String getString(ASN1Encodable attributeValue) {
		String string;
		if (attributeValue instanceof ASN1String) {
			string = ((ASN1String) attributeValue).getString();
		} else if (attributeValue instanceof ASN1ObjectIdentifier) {
			string = ((ASN1ObjectIdentifier) attributeValue).getId();
		} else {
			LOG.error("!!!*******!!! This encoding is unknown: {}", attributeValue.getClass().getSimpleName());
			string = attributeValue.toString();
			LOG.error("!!!*******!!! value: {}", string);
		}
		return string;
	}

	/**
	 * Extract attribute with the {@code identifier} from {@code X500PrincipalHelper}
	 *
	 * @param identifier {@link ASN1ObjectIdentifier} oid of the attribute to get value
	 * @param principal {@link X500PrincipalHelper} to extract the attribute value from
	 * @return {@link String} value
	 */
	public static String extractAttributeFromX500Principal(ASN1ObjectIdentifier identifier, X500PrincipalHelper principal) {
		final X500Name x500Name = X500Name.getInstance(principal.getEncoded());
		RDN[] rdns = x500Name.getRDNs(identifier);
		for (RDN rdn : rdns) {
			if (rdn.isMultiValued()) {
				AttributeTypeAndValue[] typesAndValues = rdn.getTypesAndValues();
				for (AttributeTypeAndValue typeAndValue : typesAndValues) {
					if (identifier.equals(typeAndValue.getType())) {
						return typeAndValue.getValue().toString();
					}
				}
			} else {
				AttributeTypeAndValue typeAndValue = rdn.getFirst();
				if (identifier.equals(typeAndValue.getType())) {
					return typeAndValue.getValue().toString();
				}
			}
		}
		return null;
	}

	/**
	 * Extracts the Subject Common name from the certificate token
	 *
	 * @param cert {@link CertificateToken}
	 * @return {@link String}
	 */
	public static String getSubjectCommonName(CertificateToken cert) {
		return extractAttributeFromX500Principal(BCStyle.CN, cert.getSubject());
	}

	/**
	 * Extracts the pretty printed name of the certificate token
	 *
	 * @param cert {@link CertificateToken}
	 * @return {@link String}
	 */
	public static String getHumanReadableName(CertificateToken cert) {
		return firstNotNull(cert, BCStyle.CN, BCStyle.GIVENNAME, BCStyle.SURNAME, BCStyle.NAME,
				BCStyle.PSEUDONYM, BCStyle.O, BCStyle.OU);
	}

	private static String firstNotNull(CertificateToken cert, ASN1ObjectIdentifier... oids) {
		for (ASN1ObjectIdentifier oid : oids) {
			String value = extractAttributeFromX500Principal(oid, cert.getSubject());
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Returns the first {@code SignerInformation} extracted from {@code CMSSignedData}.
	 *
	 * @param cms
	 *            CMSSignedData
	 * @return returns {@code SignerInformation}
	 */
	public static SignerInformation getFirstSignerInformation(final CMSSignedData cms) {
		final Collection<SignerInformation> signers = cms.getSignerInfos().getSigners();
		if (signers.size() > 1) {
			LOG.warn("!!! The framework handles only one signer (SignerInformation) !!!");
		}
		return signers.iterator().next();
	}

	/**
	 * Checks if the byte defines an ASN1 Sequence
	 *
	 * @param tagByte byte to check
	 * @return TRUE if the byte defines an ASN1 Sequence, FALSE otherwise
	 */
	public static boolean isASN1SequenceTag(byte tagByte) {
		// BERTags.SEQUENCE | BERTags.CONSTRUCTED = 0x30
		return (BERTags.SEQUENCE | BERTags.CONSTRUCTED) == tagByte;
	}

	/**
	 * Reads the {@code encodable} and returns a {@code Date}
	 *
	 * @param encodable {@link ASN1Encodable} to read
	 * @return {@link Date}
	 */
	public static Date getDate(ASN1Encodable encodable) {
		try {
			return Time.getInstance(encodable).getDate();
		} catch (Exception e) {
			LOG.warn("Unable to retrieve the date {}", encodable, e);
			return null;
		}
	}

	/**
	 * Checks if the {@code attributeTable} is empty
	 *
	 * @param attributeTable {@link AttributeTable}
	 * @return TRUE if the attribute table is empty, FALSE otherwise
	 */
	public static boolean isEmpty(AttributeTable attributeTable) {
		return (attributeTable == null) || (attributeTable.size() == 0);
	}

	/**
	 * Returns the current {@code originalAttributeTable} if instantiated, an empty {@code AttributeTable} if null
	 *
	 * @param originalAttributeTable {@link AttributeTable}
	 * @return {@link AttributeTable}
	 */
	public static AttributeTable emptyIfNull(AttributeTable originalAttributeTable) {
		if (originalAttributeTable == null) {
			return new AttributeTable(new Hashtable<ASN1ObjectIdentifier, Attribute>());
		} else {
			return originalAttributeTable;
		}
	}

	/**
	 * Extracts all extended key usages for the certificate token
	 *
	 * @param certToken {@link CertificateToken}
	 * @return a list of {@link String}s
	 */
	public static List<String> getExtendedKeyUsage(CertificateToken certToken) {
		try {
			return certToken.getCertificate().getExtendedKeyUsage();
		} catch (CertificateParsingException e) {
			LOG.warn("Unable to retrieve ExtendedKeyUsage : {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Gets the {@code IssuerSerial} object
	 *
	 * @param binaries representing the {@link IssuerSerial}
	 * @return {@link IssuerSerial} if able to parse, null otherwise
	 */
	public static IssuerSerial getIssuerSerial(byte[] binaries) {
		try (ASN1InputStream is = new ASN1InputStream(binaries)) {
			ASN1Sequence seq = (ASN1Sequence) is.readObject();
			return IssuerSerial.getInstance(seq);
		} catch (Exception e) {
			LOG.error("Unable to decode IssuerSerialV2 textContent '{}' : {}", Utils.toBase64(binaries), e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Transforms an object of class {@code IssuerSerial} into instance of
	 * {@code CertificateIdentifier}
	 * 
	 * @param issuerAndSerial {@link IssuerSerial} to transform
	 * @return {@link CertificateIdentifier}
	 */
	public static CertificateIdentifier toCertificateIdentifier(IssuerSerial issuerAndSerial) {
		if (issuerAndSerial == null) {
			return null;
		}
		try {
			CertificateIdentifier certificateIdentifier = new CertificateIdentifier();
			GeneralNames gnames = issuerAndSerial.getIssuer();
			if (gnames != null) {
				GeneralName[] names = gnames.getNames();
				if (names.length == 1) {
					certificateIdentifier.setIssuerName(new X500Principal(names[0].getName().toASN1Primitive().getEncoded(ASN1Encoding.DER)));
				} else {
					LOG.warn("More than one GeneralName");
				}
			}

			ASN1Integer serialNumber = issuerAndSerial.getSerial();
			if (serialNumber != null) {
				certificateIdentifier.setSerialNumber(serialNumber.getValue());
			}

			return certificateIdentifier;
		} catch (Exception e) {
			LOG.error("Unable to read the IssuerSerial object", e);
			return null;
		}
	}

	/**
	 * Returns ats-hash-index table, with a related version present in from timestamp's unsigned properties
	 * 
	 * @param timestampUnsignedAttributes {@link AttributeTable} unsigned properties of the timestamp
	 * @return the content of SignedAttribute: ATS-hash-index unsigned attribute with a present version
	 */
	public static ASN1Sequence getAtsHashIndex(AttributeTable timestampUnsignedAttributes) {
		ASN1ObjectIdentifier atsHashIndexVersionIdentifier = getAtsHashIndexVersionIdentifier(timestampUnsignedAttributes);
		return getAtsHashIndexByVersion(timestampUnsignedAttributes, atsHashIndexVersionIdentifier);
	}

	/**
	 * Returns ats-hash-index table, with a specified version present in from timestamp's unsigned properties
	 * 
	 * @param timestampUnsignedAttributes {@link AttributeTable} unsigned properties of the timestamp
	 * @param atsHashIndexVersionIdentifier {@link ASN1ObjectIdentifier} identifier of ats-hash-index table to get
	 * @return the content of SignedAttribute: ATS-hash-index unsigned attribute with a requested version if present
	 */
	public static ASN1Sequence getAtsHashIndexByVersion(AttributeTable timestampUnsignedAttributes, 
			ASN1ObjectIdentifier atsHashIndexVersionIdentifier) {
		if (timestampUnsignedAttributes != null && atsHashIndexVersionIdentifier != null) {
			final Attribute atsHashIndexAttribute = timestampUnsignedAttributes.get(atsHashIndexVersionIdentifier);
			if (atsHashIndexAttribute != null) {
				final ASN1Set attrValues = atsHashIndexAttribute.getAttrValues();
				if (attrValues != null && attrValues.size() == 1) {
					return (ASN1Sequence) attrValues.getObjectAt(0).toASN1Primitive();
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns {@code ASN1ObjectIdentifier} of the found AtsHashIndex
	 * @param timestampUnsignedAttributes {@link AttributeTable} of the timestamp's unsignedAttributes
	 * @return {@link ASN1ObjectIdentifier} of the AtsHashIndex element version
	 */
	public static ASN1ObjectIdentifier getAtsHashIndexVersionIdentifier(AttributeTable timestampUnsignedAttributes) {
		if (timestampUnsignedAttributes != null) {
			Attributes attributes = timestampUnsignedAttributes.toASN1Structure();
			for (Attribute attribute : attributes.getAttributes()) {
				ASN1ObjectIdentifier attrType = attribute.getAttrType();
				if (id_aa_ATSHashIndex.equals(attrType) || id_aa_ATSHashIndexV2.equals(attrType) || id_aa_ATSHashIndexV3.equals(attrType)) {
					LOG.debug("Unsigned attribute of type [{}] found in the timestamp.", attrType);
					return attrType;
				}
			}
			LOG.warn("The timestamp unsignedAttributes does not contain ATSHashIndex!");
		}
		return null;
	}
	
	/**
	 * Returns octets from the given attribute by defined atsh-hash-index type
	 * 
	 * @param attribute                     {@link Attribute} to get byte array from
	 * @param atsHashIndexVersionIdentifier {@link ASN1ObjectIdentifier} to specify
	 *                                      rules
	 * @return byte array
	 */
	public static List<byte[]> getOctetStringForAtsHashIndex(Attribute attribute, ASN1ObjectIdentifier atsHashIndexVersionIdentifier) {
		/*
		 *  id_aa_ATSHashIndexV3 (EN 319 122-1 v1.1.1) -> Each one shall contain the hash
		 *  value of the octets resulting from concatenating the Attribute.attrType field and one of the instances of
		 *  AttributeValue within the Attribute.attrValues within the unsignedAttrs field. One concatenation
		 *  operation shall be performed as indicated above, and the hash value of the obtained result included in
		 *  unsignedAttrsHashIndex
		 */
		if (id_aa_ATSHashIndexV3.equals(atsHashIndexVersionIdentifier)) {
			return getATSHashIndexV3OctetString(attribute.getAttrType(), attribute.getAttrValues());
		} else {
			/*
			 * id_aa_ATSHashIndex (TS 101 733 v2.2.1) and id_aa_ATSHashIndexV2 (EN 319 122-1 v1.0.0) ->
			 * The field unsignedAttrsHashIndex shall be a sequence of octet strings. Each one shall contain the hash value of
			 * one instance of Attribute within the unsignedAttrs field of the SignerInfo.
			 */
			return Arrays.asList(getDEREncoded(attribute));
		}
	}

	/**
	 * Returns octets from the given attribute for ATS-Hash-Index-v3 table
	 * 
	 * @param attributeIdentifier {@link ASN1ObjectIdentifier} of the corresponding
	 *                            Attribute
	 * @param attributeValues     {@link ASN1Set} of the corresponding Attribute
	 * @return byte array representing an octet string
	 */
	public static List<byte[]> getATSHashIndexV3OctetString(ASN1ObjectIdentifier attributeIdentifier,
			ASN1Set attributeValues) {
		List<byte[]> octets = new ArrayList<>();
		byte[] attrType = getDEREncoded(attributeIdentifier);
		for (ASN1Encodable asn1Encodable : attributeValues.toArray()) {
			octets.add(DSSUtils.concatenate(attrType, getDEREncoded(asn1Encodable)));
		}
		return octets;
	}
	
	/**
	 * Returns {@link ASN1Encodable} for a given {@code oid} found in the {@code unsignedAttributes}
	 *
	 * @param attributeTable {@link AttributeTable}
	 * @param oid target {@link ASN1ObjectIdentifier}
	 * @return {@link ASN1Encodable}
	 */
	public static ASN1Encodable getAsn1Encodable(AttributeTable attributeTable, ASN1ObjectIdentifier oid) {
		final ASN1Set attrValues = getAsn1AttributeSet(attributeTable, oid);
		if (attrValues == null || attrValues.size() <= 0) {
			return null;
		}
		return attrValues.getObjectAt(0);
	}
	
	/**
	 * Returns an Attribute values for a given {@code oid} found in the {@code unsignedAttributes}
	 *
	 * @param attributeTable {@link AttributeTable}
	 * @param oid target {@link ASN1ObjectIdentifier}
	 * @return {@link ASN1Set}
	 */
	public static ASN1Set getAsn1AttributeSet(AttributeTable attributeTable, ASN1ObjectIdentifier oid) {
		final Attribute attribute = attributeTable.get(oid);
		if (attribute == null) {
			return null;
		}
		return attribute.getAttrValues();
	}
	
	/**
	 * Returns an array of {@link Attribute}s for a given {@code oid} found in the {@code unsignedAttributes}
	 *
	 * @param attributeTable {@link AttributeTable}
	 * @param oid target {@link ASN1ObjectIdentifier}
	 * @return {@link Attribute}s array
	 */
	public static Attribute[] getAsn1Attributes(AttributeTable attributeTable, ASN1ObjectIdentifier oid) {
		ASN1EncodableVector encodableVector = attributeTable.getAll(oid);
		if (encodableVector == null) {
			return null;
		}
		Attributes attributes = new Attributes(encodableVector);
		return attributes.getAttributes();
	}
	
	/**
	 * Finds archive {@link TimeStampToken}s
	 *
	 * @param unsignedAttributes {@link AttributeTable} to obtain timestamps from
	 * @return a list of {@link TimeStampToken}s
	 */
	public static List<TimeStampToken> findArchiveTimeStampTokens(AttributeTable unsignedAttributes) {
		List<TimeStampToken> timeStamps = new ArrayList<>();
		Attribute[] attributes = unsignedAttributes.toASN1Structure().getAttributes();
		for (final Attribute attribute : attributes) {
			if (isArchiveTimeStampToken(attribute)) {
				TimeStampToken timeStampToken = getTimeStampToken(attribute);
				if (timeStampToken != null) {
					timeStamps.add(timeStampToken);
				}
			}
		}
		return timeStamps;
	}
	
	/**
	 * Returns a list of all CMS timestamp identifiers
	 * 
	 * @return a list of {@link ASN1ObjectIdentifier}s
	 */
	public static List<ASN1ObjectIdentifier> getTimestampOids() {
		return timestampOids;
	}

	/**
	 * Checks if the attribute is of an allowed archive timestamp type
	 * 
	 * @param attribute {@link Attribute} to check
	 * @return true if the attribute represents an archive timestamp element, false
	 *         otherwise
	 */
	public static boolean isArchiveTimeStampToken(Attribute attribute) {
		return isAttributeOfType(attribute, OID.id_aa_ets_archiveTimestampV2) || isAttributeOfType(attribute, OID.id_aa_ets_archiveTimestampV3);
	}
	
	/**
	 * Checks if the given attribute is an instance of the expected asn1ObjectIdentifier type
	 * 
	 * @param attribute {@link Attribute} to check
	 * @param asn1ObjectIdentifier {@link ASN1ObjectIdentifier} type to check against
	 * @return TRUE if the attribute is of type asn1ObjectIdentifier, FALSE otherwise
	 */
	public static boolean isAttributeOfType(Attribute attribute, ASN1ObjectIdentifier asn1ObjectIdentifier) {
		if (attribute == null) {
			return false;
		}
		ASN1ObjectIdentifier objectIdentifier = attribute.getAttrType();
		return asn1ObjectIdentifier.equals(objectIdentifier);
	}
	
	/**
	 * Creates a TimeStampToken from the provided {@code attribute}
	 *
	 * @param attribute {@link Attribute} to generate {@link TimeStampToken} from
	 * @return {@link TimeStampToken}
	 */
	public static TimeStampToken getTimeStampToken(Attribute attribute) {
		try {
			CMSSignedData signedData = getCMSSignedData(attribute);
			if (signedData != null) {
				return new TimeStampToken(signedData);
			}
		} catch (IOException | CMSException | TSPException e) {
			LOG.warn("The given TimeStampToken cannot be created! Reason: [{}]", e.getMessage());
		}
		return null;
	}

	/**
	 * Creates a CMSSignedData from the provided {@code attribute}
	 *
	 * @param attribute {@link Attribute} to generate {@link CMSSignedData} from
	 * @return {@link CMSSignedData}
	 * @throws IOException in case of encoding exception
	 * @throws CMSException in case if the provided {@code attribute} cannot be converted to {@link CMSSignedData}
	 */
	public static CMSSignedData getCMSSignedData(Attribute attribute) throws CMSException, IOException {
		ASN1Encodable value = getAsn1Encodable(attribute);
		if (value instanceof DEROctetString) {
			LOG.warn("Illegal content for CMSSignedData (OID : {}) : OCTET STRING is not allowed !", attribute.getAttrType());
		} else {
			ASN1Primitive asn1Primitive = value.toASN1Primitive();
			return new CMSSignedData(asn1Primitive.getEncoded());
		}
		return null;
	}
	
	/**
	 * Returns {@code ASN1Encodable} of the {@code attribute}
	 *
	 * @param attribute {@link Attribute}
	 * @return {@link ASN1Encodable}
	 */
	public static ASN1Encodable getAsn1Encodable(Attribute attribute) {
		return attribute.getAttrValues().getObjectAt(0);
	}
	
	/**
	 * Returns generation time for the provided {@code timeStampToken}
	 *
	 * @param timeStampToken {@link TimeStampToken} to get generation time for
	 * @return {@link Date} timestamp generation time
	 */
	public static Date getTimeStampTokenGenerationTime(TimeStampToken timeStampToken) {
		if (timeStampToken != null) {
			return timeStampToken.getTimeStampInfo().getGenTime();
		}
		return null;
	}

	/**
	 * Returns {@link RevocationValues} from the given encodable
	 * 
	 * @param encodable
	 *                  the encoded data to be parsed
	 * @return an instance of RevocationValues or null if the parsing failed
	 */
	public static RevocationValues getRevocationValues(ASN1Encodable encodable) {
		if (encodable != null) {
			try {
				return RevocationValues.getInstance(encodable);
			} catch (Exception e) {
				LOG.warn("Unable to parse RevocationValues", e);
			}
		}
		return null;
	}

	/**
	 * Converts the {@code OtherCertID} to {@code CertificateRef}
	 *
	 * @param otherCertId {@link OtherCertID}
	 * @return {@link CertificateRef}
	 */
	public static CertificateRef getCertificateRef(OtherCertID otherCertId) {
		CertificateRef certRef = new CertificateRef();
		DigestAlgorithm digestAlgo = DigestAlgorithm.forOID(otherCertId.getAlgorithmHash().getAlgorithm().getId());
		certRef.setCertDigest(new Digest(digestAlgo, otherCertId.getCertHash()));
		certRef.setCertificateIdentifier(toCertificateIdentifier(otherCertId.getIssuerSerial()));
		return certRef;
	}

	/**
	 * This method extract the PSD2 QcStatement informations for a given certificate
	 * 
	 * @param certToken the certificate
	 * @return an instance of {@code PSD2QcType} or null
	 */
	public static PSD2QcType getPSD2QcStatement(CertificateToken certToken) {
		PSD2QcType result = null;
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				for (int i = 0; i < seq.size(); i++) {
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(i));
					if (OID.psd2_qcStatement.equals(statement.getStatementId())) {
						result = new PSD2QcType();
						ASN1Sequence psd2Seq = ASN1Sequence.getInstance(statement.getStatementInfo());
						ASN1Sequence rolesSeq = ASN1Sequence.getInstance(psd2Seq.getObjectAt(0));

						List<RoleOfPSP> rolesOfPSP = new ArrayList<>();
						for (int ii = 0; ii < rolesSeq.size(); ii++) {
							ASN1Sequence oneRoleSeq = ASN1Sequence.getInstance(rolesSeq.getObjectAt(ii));
							RoleOfPSP roleOfPSP = new RoleOfPSP();
							ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) oneRoleSeq.getObjectAt(0);
							roleOfPSP.setPspOid(RoleOfPspOid.fromOid(oid.getId()));
							roleOfPSP.setPspName(getString(oneRoleSeq.getObjectAt(1)));
							rolesOfPSP.add(roleOfPSP);
						}
						result.setRolesOfPSP(rolesOfPSP);
						result.setNcaName(getString(psd2Seq.getObjectAt(1)));
						result.setNcaId(getString(psd2Seq.getObjectAt(2)));
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to read QCStatement", e);
			}
		}
		return result;
	}

	/**
	 * This method extracts the QcStatement regarding limits on the value of transactions for a given certificate
	 *
	 * @param certToken the certificate
	 * @return an instance of {@code QCLimitValue} or null
	 */
	public static QCLimitValue getQcLimitValue(CertificateToken certToken) {
		QCLimitValue result = null;
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				for (int i = 0; i < seq.size(); i++) { 
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(i));
					if (ETSIQCObjectIdentifiers.id_etsi_qcs_LimiteValue.equals(statement.getStatementId())) {
						MonetaryValue monetaryValue = MonetaryValue.getInstance(statement.getStatementInfo());
						result = new QCLimitValue();
						result.setCurrency(monetaryValue.getCurrency().getAlphabetic());
						result.setAmount(monetaryValue.getAmount().intValue());
						result.setExponent(monetaryValue.getExponent().intValue());
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to read QCStatement", e);
			}
		}
		return result;
	}

	/**
	 * Returns a list of subject alternative names
	 *
	 * @param certToken {@link CertificateToken}
	 * @return a list of {@link String}s
	 */
	public static List<String> getSubjectAlternativeNames(CertificateToken certToken) {
		List<String> result = new ArrayList<>();
		try {
			Collection<List<?>> subjectAlternativeNames = certToken.getCertificate().getSubjectAlternativeNames();
			if (Utils.isCollectionNotEmpty(subjectAlternativeNames)) {
				for (List<?> list : subjectAlternativeNames) {
					// type + value
					if (Utils.collectionSize(list) == 2) {
						Object value = list.get(1);
						if (value instanceof String) {
							result.add((String) value);
						} else {
							LOG.trace("Ignored value : {}", value);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.warn("Unable to extract SubjectAlternativeNames", e);
		}
		return result;
	}

	/**
	 * Extracts the {@code SemanticsIdentifier} from the certificate token
	 *
	 * @param certToken {@link CertificateToken}
	 * @return {@link SemanticsIdentifier}
	 */
	public static SemanticsIdentifier getSemanticsIdentifier(CertificateToken certToken) {
		final byte[] qcStatement = certToken.getCertificate().getExtensionValue(Extension.qCStatements.getId());
		if (Utils.isArrayNotEmpty(qcStatement)) {
			try {
				final ASN1Sequence seq = getAsn1SequenceFromDerOctetString(qcStatement);
				for (int i = 0; i < seq.size(); i++) {
					final QCStatement statement = QCStatement.getInstance(seq.getObjectAt(i));
					if (RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v2.equals(statement.getStatementId())) {
						SemanticsInformation semanticsInfo = SemanticsInformation.getInstance(statement.getStatementInfo());
						if (semanticsInfo != null && semanticsInfo.getSemanticsIdentifier() != null) {
							return SemanticsIdentifier.fromOid(semanticsInfo.getSemanticsIdentifier().getId());
						}
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to extract the SemanticsIdentifier", e);
			}
		}
		return null;
	}
	
	/**
	 * Converts the ANS.1 binary signature value to the concatenated R || S format
	 * 
	 * NOTE: used in XAdES and JAdES
	 *
	 * @param algorithm
	 *            Encryption algorithm used to create the signatureValue
	 * @param signatureValue
	 *            the originally computed signature value
	 * @return the converted signature value
	 */
	public static byte[] fromAsn1toSignatureValue(final EncryptionAlgorithm algorithm, byte[] signatureValue) {
		if ((EncryptionAlgorithm.ECDSA == algorithm || EncryptionAlgorithm.DSA == algorithm) && isAsn1Encoded(signatureValue)) {
			return fromAsn1ToDsaRS(signatureValue);
		} else {
			return signatureValue;
		}
	}

	/**
	 * Converts an ASN.1 value to a concatenation string of R and S from ECDSA/DSA encryption algorithm
	 *
	 * The JAVA JCE ECDSA/DSA Signature algorithm creates ASN.1 encoded (r,s) value pairs.
	 *
	 * @param asn1SignatureValue
	 *            the ASN1 signature value
	 * @return the decode bytes
	 * @see <A HREF="http://www.w3.org/TR/xmldsig-core/#dsa-sha1">6.4.1 DSA</A>
	 * @see <A HREF="ftp://ftp.rfc-editor.org/in-notes/rfc4050.txt">3.3. ECDSA Signatures</A>
	 */
	private static byte[] fromAsn1ToDsaRS(byte[] asn1SignatureValue) {

		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); ASN1InputStream is = new ASN1InputStream(asn1SignatureValue)) {

			ASN1Sequence seq = (ASN1Sequence) is.readObject();
			if (seq.size() != 2) {
				throw new IllegalArgumentException("ASN1 Sequence size should be 2 !");
			}

			ASN1Integer r = (ASN1Integer) seq.getObjectAt(0);
			ASN1Integer s = (ASN1Integer) seq.getObjectAt(1);

			byte[] rBytes = BigIntegers.asUnsignedByteArray(r.getValue());
			int rSize = rBytes.length;
			byte[] sBytes = BigIntegers.asUnsignedByteArray(s.getValue());
			int sSize = sBytes.length;
			int max = Math.max(rSize, sSize);
			max = max % 2 == 0 ? max : max + 1;
			leftPad(buffer, max, rBytes);
			buffer.write(rBytes);
			leftPad(buffer, max, sBytes);
			buffer.write(sBytes);

			return buffer.toByteArray();
		} catch (Exception e) {
			throw new DSSException("Unable to convert to xmlDsig : " + e.getMessage(), e);
		}
	}

	private static void leftPad(final ByteArrayOutputStream stream, final int size, final byte[] array) {
		final int diff = size - array.length;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				stream.write(0x00);
			}
		}
	}

	/**
	 * Checks if the binaries are ASN.1 encoded.
	 *
	 * @param binaries
	 *            byte array to check.
	 * @return if the binaries are ASN.1 encoded.
	 */
	public static boolean isAsn1Encoded(byte[] binaries) {
		try (ASN1InputStream is = new ASN1InputStream(binaries)) {
			ASN1Sequence seq = (ASN1Sequence) is.readObject();
			return seq != null && seq.size() == 2;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns a value of an ASN.1 DirectoryString instance Returns null if an error
	 * occurs during the transformation
	 * 
	 * @param directoryStringInstance {@link ASN1Encodable} to get DirectoryString
	 *                                value from
	 * @return {@link String} value
	 */
	public static String getDirectoryStringValue(ASN1Encodable directoryStringInstance) {
		String postalAddress = null;
		try {
			DirectoryString directoryString = DirectoryString.getInstance(directoryStringInstance);
			postalAddress = directoryString.getString();
		} catch (Exception e) {
			String errorMessage = "Unable to build a DirectoryString instance. Reason : {}";
			if (LOG.isDebugEnabled()) {
				LOG.warn(errorMessage, e.getMessage(), e);
			} else {
				LOG.warn(errorMessage, e.getMessage());
			}
		}
		return postalAddress;
	}

}
