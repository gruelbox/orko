package com.gruelbox.orko.job.script;

import static com.gruelbox.orko.job.script.Script.TABLE_NAME;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.inject.Provider;

class ScriptAccess {

  private final Provider<SessionFactory> sessionFactory;

  @Inject
  ScriptAccess(Provider<SessionFactory> sf) {
    this.sessionFactory = sf;
  }

  public void insert(Script script) {
    session().save(script);
  }

  public List<Script> list() {
    return session().createQuery("from " + TABLE_NAME, Script.class).list();
  }

  public void update(Script script) {
    session().update(script);
  }

  public void delete(String id) {
    session()
      .createQuery("delete from " + TABLE_NAME + " where id = :id")
      .setParameter("id", id)
      .executeUpdate();
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }
}