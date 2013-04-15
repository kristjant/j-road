package ee.webmedia.xtee.client.tam;

import ee.webmedia.xtee.client.exception.XTeeServiceConsumptionException;
import ee.webmedia.xtee.client.tam.types.ee.riik.xtee.tam.producers.producer.tam.Registriisik;
import java.util.Calendar;
import java.util.List;


/**
 * Queries to Health Board (Terviseamet -- TAM)
 * 
 * @author Klaus-Eduard Runnel
 */
public interface TamXTeeService {
  /**
   * <code>tam.registriisik</code> service.
   */
  List<Registriisik> findRegistriisik(String kood) throws XTeeServiceConsumptionException;

  /**
   * <code>tam.tervishoiutootajamuudatuskood</code> service.
   */
  // TODO: types are integers, should use enum map?
  List<String> findTervishoiutootajamuudatuskood(Calendar startDate, Calendar endDate, int... types) throws XTeeServiceConsumptionException;
}
