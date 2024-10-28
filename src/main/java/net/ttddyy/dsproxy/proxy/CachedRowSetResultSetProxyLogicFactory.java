package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.DataSourceProxyException;
import net.ttddyy.dsproxy.QueryInfo;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Factory to create {@link CachedRowSetResultSetProxyLogic}.
 *
 * Provide a {@link ResultSet} proxy using {@link CachedRowSet} which provides disconnected scrollability.
 *
 * This class uses {@link RowSetFactory} that requires jdk1.7+ to create a {@link CachedRowSet}.
 * You could change the creation strategy either uses mechanism of {@link RowSetFactory} or simply extend this class
 * or implement another {@link ResultSetProxyLogicFactory}.
 *
 * The default {@link CachedRowSet} implementation is {@code com.sun.rowset.CachedRowSetImpl}.
 *
 * @author Tadaya Tsuyukubo
 * @see RowSetFactory
 * @see CachedRowSet
 * @see CachedRowSetResultSetProxyLogic
 * @since 1.4.7
 */
@IgnoreJRERequirement
public class CachedRowSetResultSetProxyLogicFactory implements ResultSetProxyLogicFactory {

    private final RowSetFactory rowSetFactory;

    public CachedRowSetResultSetProxyLogicFactory() {
        try {
            this.rowSetFactory = RowSetProvider.newFactory();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ResultSetProxyLogic create(ResultSet resultSet, List<QueryInfo> queries, ConnectionInfo connectionInfo, ProxyConfig proxyConfig) {
        ResultSet cachedRowSet = getCachedRowSet(resultSet);
        return new CachedRowSetResultSetProxyLogic(resultSet, cachedRowSet, connectionInfo, proxyConfig);
    }

    protected ResultSet getCachedRowSet(ResultSet resultSet) {
        try {
            // CachedRowSet only works with non-null ResultSet
            if (resultSet.getMetaData().getColumnCount() > 0) {
                CachedRowSet cachedRowSet = this.rowSetFactory.createCachedRowSet();
                cachedRowSet.populate(resultSet);
                return cachedRowSet;
            } else {
                // for null result-set, return the given one
                return resultSet;
            }
        } catch (SQLException e) {
            throw new DataSourceProxyException("Failed to create CachedRowSet", e);
        }
    }

}
