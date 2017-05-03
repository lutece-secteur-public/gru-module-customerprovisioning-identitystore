/*
 * Copyright (c) 2002-2016, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.customerprovisioning.modules.identitystore.services;

import fr.paris.lutece.plugins.customerprovisioning.services.ICustomerInfoService;
import fr.paris.lutece.plugins.grubusiness.business.customer.Customer;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.service.IdentityService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * This class manages customer via the identity store
 *
 */
public class IdentityStoreCustomerInfoService implements ICustomerInfoService
{
    private static final String ATTRIBUTE_USER_NAME_GIVEN = "customerprovisioning.identity.attribute.user.name.given";
    private static final String ATTRIBUTE_USER_NAME_FAMILLY = "customerprovisioning.identity.attribute.user.name.family";
    private static final String ATTRIBUTE_USER_HOMEINFO_ONLINE_EMAIL = "customerprovisioning.identity.attribute.user.home-info.online.email";
    private static final String ATTRIBUTE_USER_HOMEINFO_TELECOM_TELEPHONE_NUMBER = "customerprovisioning.identity.attribute.user.home-info.telecom.telephone.number";
    private static final String ATTRIBUTE_USER_HOMEINFO_TELECOM_MOBILE_NUMBER = "customerprovisioning.identity.attribute.user.home-info.telecom.mobile.number";
    private static final String ATTRIBUTE_IDENTITY_NAME_GIVEN = AppPropertiesService.getProperty( ATTRIBUTE_USER_NAME_GIVEN );
    private static final String ATTRIBUTE_IDENTITY_NAME_FAMILLY = AppPropertiesService.getProperty( ATTRIBUTE_USER_NAME_FAMILLY );
    private static final String ATTRIBUTE_IDENTITY_HOMEINFO_ONLINE_EMAIL = AppPropertiesService.getProperty( ATTRIBUTE_USER_HOMEINFO_ONLINE_EMAIL );
    private static final String ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_TELEPHONE_NUMBER = AppPropertiesService.getProperty( ATTRIBUTE_USER_HOMEINFO_TELECOM_TELEPHONE_NUMBER );
    private static final String ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_MOBILE_NUMBER = AppPropertiesService.getProperty( ATTRIBUTE_USER_HOMEINFO_TELECOM_MOBILE_NUMBER );

    // FIXME : the application code must be provided by the caller
    private static final String APPLICATION_CODE = "CustomerProvisioning";

    //Service identityStore
    private static final String BEAN_IDENTITYSTORE_SERVICE = "customerprovisioning.identitystore.service";
    @Inject
    @Named( BEAN_IDENTITYSTORE_SERVICE )
    private IdentityService _identityService;

    /**
     * default constructor
     */
    public IdentityStoreCustomerInfoService(  )
    {
        super(  );
    }

    @Override
    public Customer getCustomerByGuid( String strGuid )
    {
        Customer customer = null;

        try
        {
            // FIXME : the hash must be provided
            IdentityDto identityDto = _identityService.getIdentity( strGuid, APPLICATION_CODE, StringUtils.EMPTY );
            customer = identityDtoToCustomer( identityDto );
        }
        catch ( IdentityNotFoundException e )
        {
            // The customer is not in the identity store yet : nothing to do
        }

        return customer;
    }

    @Override
    public Customer getCustomerByCid( String strCustomerId )
    {
        Customer customer = null;

        try
        {
            int nCustomerId = Integer.parseInt( strCustomerId );

            // FIXME : the hash must be provided
            IdentityDto identityDto = _identityService.getIdentity( nCustomerId, APPLICATION_CODE, StringUtils.EMPTY );
            customer = identityDtoToCustomer( identityDto );
        }
        catch ( NumberFormatException e )
        {
            throw new IllegalArgumentException( e );
        }
        catch ( IdentityNotFoundException e )
        {
            // The customer is not in the identity store yet : nothing to do
        }

        return customer;
    }

    @Override
    public Customer createCustomer( Customer customer )
    {
        IdentityChangeDto identityChangeDto = new IdentityChangeDto(  );
        IdentityDto identityDto = customerToIdentityDto( customer );

        identityChangeDto.setIdentity( identityDto );

        AuthorDto authorDto = new AuthorDto(  );
        authorDto.setApplicationCode( APPLICATION_CODE );

        identityChangeDto.setAuthor( authorDto );

        identityDto = _identityService.createIdentity( identityChangeDto, StringUtils.EMPTY );

        customer.setId( identityDto.getCustomerId(  ) );

        return customer;
    }

    /**
     * Converts a Customer object to an IdentityDto object
     * @param customer the Customer object
     * @return the IdentityDto object
     */
    private IdentityDto customerToIdentityDto( Customer customer )
    {
        IdentityDto identityDto = new IdentityDto(  );
        Map<String, AttributeDto> mapAttributes = new HashMap<String, AttributeDto>(  );

        identityDto.setConnectionId( customer.getAccountGuid(  ) );
        identityDto.setCustomerId( customer.getId(  ) );
        identityDto.setAttributes( mapAttributes );

        setAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_GIVEN, customer.getFirstname(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_FAMILLY, customer.getLastname(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_ONLINE_EMAIL, customer.getEmail(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_TELEPHONE_NUMBER,
            customer.getFixedPhoneNumber(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_MOBILE_NUMBER, customer.getMobilePhone(  ) );

        return identityDto;
    }

    /**
     * Converts an IdentityDto object to a Customer object
     * @param identityDto the IdentityDto object
     * @return the Customer object
     */
    private Customer identityDtoToCustomer( IdentityDto identityDto )
    {
        Customer customer = new Customer(  );

        customer.setAccountGuid( identityDto.getConnectionId(  ) );
        customer.setId( identityDto.getCustomerId(  ) );
        customer.setFirstname( getAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_GIVEN ) );
        customer.setLastname( getAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_FAMILLY ) );
        customer.setEmail( getAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_ONLINE_EMAIL ) );
        customer.setFixedPhoneNumber( getAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_TELEPHONE_NUMBER ) );
        customer.setMobilePhone( getAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_TELECOM_MOBILE_NUMBER ) );

        return customer;
    }

    /**
     * Sets an attribute into the specified identity
     * @param identityDto the identity
     * @param strCode the attribute code
     * @param strValue the attribute value
     */
    private void setAttribute( IdentityDto identityDto, String strCode, String strValue )
    {
        AttributeDto attributeDto = new AttributeDto(  );
        attributeDto.setKey( strCode );
        attributeDto.setValue( strValue );

        identityDto.getAttributes(  ).put( attributeDto.getKey(  ), attributeDto );
    }

    /**
     * Gets the attribute value from the specified identity
     * @param identityDto the identity
     * @param strCode the attribute code
     * @return {@code null} if the attribute does not exist, the attribute value otherwise
     */
    private String getAttribute( IdentityDto identityDto, String strCode )
    {
        AttributeDto attribute = identityDto.getAttributes(  ).get( strCode );

        return ( attribute == null ) ? null : attribute.getValue(  );
    }
}
