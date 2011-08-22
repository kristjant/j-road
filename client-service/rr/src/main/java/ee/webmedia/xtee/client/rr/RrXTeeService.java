package ee.webmedia.xtee.client.rr;

import ee.webmedia.xtee.client.exception.XTeeServiceConsumptionException;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.DokumendiTyyp;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR40Response;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR42Request;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR42Response;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR43Response;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR52Response;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR63ResponseV1;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR72Response.TtIsikud;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR81Response;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RR96ResponseV1;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RRExtDocumentDataRequest;
import ee.webmedia.xtee.client.rr.types.ee.riik.xtee.rr.producers.producer.rr.RRExtDocumentDataResponse;
import java.util.List;

/**
 * <code>rr</code> (Rahvastikuregister) database X-tee service.
 *
 * @author Roman Tekhov
 */
public interface RrXTeeService {

  /**
   * <code>rr.RR72_isik.v1</code> service.
   */
  List<TtIsikud.Item> findRR72Isik(String... idCodes) throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR63isikAadrDok.v1</code> service.
   */
	RR63ResponseV1 findRR63IsikAadrDok(String surname, String firstName, String idCode, String birthDate)
      throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR81KMAisikkontroll.v1</code> service.
   */
  RR81Response getRR81KMAisikkontroll(String idCode) throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR40isikTaielikIsikukood.v1</code> service.
   */
  RR40Response findRR40isikTaielikIsikukood(String isikukood)
      throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR42isikAadressKood.v1</code> service.
   */
  RR42Response findRR42isikAadressKood(RR42RequestCallback cb)
      throws XTeeServiceConsumptionException;

  interface RR42RequestCallback {
    void populate(RR42Request paring);
  }

  /**
   * <code>rr.RR52.v1</code> service.
   */
  RR52Response getRR52(String idCode, String forename, String surname) throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR43dokument.v1</code> service.
   */
  RR43Response getRR43dokument(String dokumendiKood, String dokumendiSeeria, DokumendiTyyp.Enum dokumendiTyyp) throws XTeeServiceConsumptionException;

  /**
   * <code>rr.RR96IsikDokElukSuhe_v1.v1</code> service.
   */
  RR96ResponseV1 getRR96isikDokElukSuhe(String isikueesnimi, String isikuperenimi, String isikukood, Long vastusteArv) throws XTeeServiceConsumptionException;


  /**
   * <code>rr.RRExtDocumentDataArkLuba.v1</code> service.
   */
  RRExtDocumentDataResponse sendRRExtDocumentDataArkLuba(RRExtDocumentDataRequest request) throws XTeeServiceConsumptionException;

 }
