package com.recap.xml.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class BibRecord {

    private Bib bib;
    private List<Holdings> holdings;

    @XmlElement
    public Bib getBib() {
        return bib;
    }

    public void setBib(Bib bib) {
        this.bib = bib;
    }

    @XmlElement
    public List<Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holdings> holdings) {
        this.holdings = holdings;
    }

}
